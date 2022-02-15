import inspect
import threading
import time
import typing

from loguru import logger
from pampy import match, TAIL, _
from pyarrow import Schema

from core.models import ControlElement, DataElement
from core.util import get_one_of, set_one_of, StoppableQueueBlockingRunnable, IQueue, DoubleBlockingQueue
from core.util.arrow_utils import from_arrow_schema
from proto.edu.uci.ics.amber.engine.architecture.sendsemantics import *
from proto.edu.uci.ics.amber.engine.architecture.worker import *
from proto.edu.uci.ics.amber.engine.common import *
from pyamber.worker_proxy import WorkerProxy


class Controller(StoppableQueueBlockingRunnable):
    def __init__(self, workflow, input_queue: DoubleBlockingQueue):
        super().__init__(self.__class__.__name__, input_queue)
        self.available_user_commands = {'pause': PauseWorkerV2(), 'resume': ResumeWorkerV2(),
                                        'stats': QueryStatisticsV2(), 'debug': DebugCommandV2()}
        self._workflow = workflow
        self._input_queue = input_queue
        self._worker_status = {}
        self._worker_proxies: Dict[str, WorkerProxy] = dict()
        self._running = True
        self.initialize()

    def receive(self, next_entry: IQueue.QueueElement):
        match(
            next_entry,
            tuple, self.process_user_command,
            _, self.process

        )

    def process(self, msg: ControlElement):
        self.process_control_payload(msg.tag, msg.payload)

    def process_control_payload(self, tag: ActorVirtualIdentity, payload: ControlPayloadV2) -> None:
        """
        Process the given ControlPayload with the tag.
        :param tag: ActorVirtualIdentity, the sender.
        :param payload: ControlPayloadV2 to be handled.
        """
        match(
            (tag, get_one_of(payload)),
            typing.Tuple[ActorVirtualIdentity, ControlInvocationV2], self._process_control_invocation,
            typing.Tuple[ActorVirtualIdentity, ReturnInvocationV2], self._process_control_return
        )

    def _process_control_return(self, tag, return_invocation: ReturnInvocationV2):
        if return_invocation.control_return.worker_state:
            self._worker_status[tag] = return_invocation.control_return.worker_state
        elif return_invocation.control_return.worker_statistics:
            statistics = return_invocation.control_return.worker_statistics
            self._worker_status[tag] = statistics.worker_state
            print(f"{tag}:{self._worker_status[tag]}-{statistics.input_tuple_count}/{statistics.output_tuple_count}")

    def _process_control_invocation(self, tag, control_invocation: ControlInvocationV2):
        command = get_one_of(control_invocation.command)
        logger.debug(command)
        if command == WorkerExecutionCompletedV2():
            self._worker_status[tag] = "Done"
            if all(i == "Done" for i in self._worker_status.values()):
                for proxy in self._worker_proxies.values():
                    proxy.process.kill()
                    logger.debug(f"killed {proxy.id}")
                self._running = False
        elif isinstance(command, DebugPromptV2):
            print(command.msg)

    def broadcast(self, cmd, targets=None):
        if isinstance(cmd, tuple):
            cmd = self.available_user_commands[cmd[0]]
        if targets is None:
            target_proxies = self._worker_proxies.values()
        else:
            target_proxies = [self._worker_proxies[target] for target in targets]
        for target_proxy in target_proxies:
            target_proxy.send_cmd(cmd)

    def initialize(self):
        def message_forwarder(worker_proxy):
            while True:
                msg = worker_proxy._output_queue.get()
                if isinstance(msg, DataElement):
                    vid = msg.tag
                    dst_id = vid.name
                    if dst_id != "CONTROLLER":
                        dst_worker_proxy = self._worker_proxies[dst_id]
                        dst_worker_proxy.send_data(msg)
                elif isinstance(msg, ControlElement):
                    msg.tag = ActorVirtualIdentity(worker_proxy.id)
                    self._input_queue.put(msg)

        for oid, operator in self._workflow.operators.items():
            worker_proxy = WorkerProxy(oid)
            self._worker_proxies[oid] = worker_proxy
            time.sleep(1)
            is_source = operator.is_source
            code = """
from pytexera import *
from typing import Union, Optional, Iterator           
""" + inspect.getsource(operator.__class__)

            operator.init_output_schema()
            output_schema: Schema = operator.output_schema

            worker_proxy.send_cmd(
                InitializeOperatorLogicV2(code=code, is_source=is_source,
                                          output_schema=from_arrow_schema(output_schema)))
            worker_proxy.send_cmd(OpenOperatorV2())

        for oid, worker_proxy in dict(self._worker_proxies).items():
            threading.Thread(target=message_forwarder, args=(worker_proxy,), daemon=True).start()

        for lid, link in self._workflow.links.items():
            src_op_proxy = self._worker_proxies[link.from_]
            partitioning = set_one_of(Partitioning, OneToOnePartitioning(1, [ActorVirtualIdentity(link.to)]))
            link_id = LinkIdentity(from_=LayerIdentity("", link.from_, ""), to=LayerIdentity("", link.to, ""))
            src_op_proxy.send_cmd(UpdateInputLinkingV2(ActorVirtualIdentity(link.from_), link_id))
            src_op_proxy.send_cmd(AddPartitioningV2(link_id, partitioning))

        for oid, operator in self._workflow.operators.items():
            worker_proxy = self._worker_proxies[oid]
            if operator.is_source:
                worker_proxy.send_cmd(StartWorkerV2())
            worker_proxy.send_cmd(QueryStatisticsV2())

    def process_user_command(self, msg: typing.Tuple[str]):
        match(
            msg,
            ("pause",), self.broadcast,
            ("resume",), self.broadcast,
            ("stats",), self.broadcast,
            ("debug", TAIL), lambda _: self.broadcast(DebugCommandV2(' '.join(msg[2:])), [msg[1]])
        )
