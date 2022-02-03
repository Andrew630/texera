import inspect
import socket
import subprocess
import threading
import time
import typing
import uuid
from contextlib import closing
from queue import Queue

from loguru import logger
from pampy import match
from pyarrow import Schema

from core.models import InternalQueue, ControlElement, DataElement, Operator, EndOfUpstream
from core.runnables import NetworkReceiver, NetworkSender
from core.util import set_one_of, get_one_of
from core.util.arrow_utils import from_arrow_schema
from proto.edu.uci.ics.amber.engine.architecture.sendsemantics import Partitioning, OneToOnePartitioning
from proto.edu.uci.ics.amber.engine.architecture.worker import ControlCommandV2, InitializeOperatorLogicV2, \
    AddPartitioningV2, OpenOperatorV2, StartWorkerV2, UpdateInputLinkingV2, WorkerExecutionCompletedV2, \
    QueryStatisticsV2
from proto.edu.uci.ics.amber.engine.common import ControlPayloadV2, ControlInvocationV2, ActorVirtualIdentity, \
    LinkIdentity, LayerIdentity, ReturnInvocationV2


def find_free_port():
    with closing(socket.socket(socket.AF_INET, socket.SOCK_STREAM)) as s:
        s.bind(('', 0))
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        return s.getsockname()[1]


def gen_uuid(prefix=""):
    return f"{prefix}{'-' if prefix else ''}{uuid.uuid1()}"


class Link:
    def __init__(self, from_: str, to: str):
        self.from_ = from_
        self.to = to


class WorkerProxy:
    def __init__(self):
        self.id = gen_uuid("Worker")
        self.input_port = find_free_port()
        self.output_port = find_free_port()
        self._input_queue = InternalQueue()
        self._output_queue = InternalQueue()
        self.process = subprocess.Popen(
            ["python", "../../texera_run_python_worker.py", str(self.input_port), str(self.output_port), "ERROR"])
        connected = False
        while not connected:
            try:
                time.sleep(1)
                logger.debug(f"trying to connect input_port={self.input_port}, output_port={self.output_port}")
                self.network_receiver = NetworkReceiver(self._output_queue, host="0.0.0.0", port=self.output_port)
                threading.Thread(target=self.network_receiver.run, daemon=True).start()
                self.network_sender = NetworkSender(self._input_queue, host="0.0.0.0", port=self.input_port)
                threading.Thread(target=self.network_sender.run, daemon=True).start()
                connected = True
            except Exception as err:
                logger.error(err)

    def send_cmd(self, cmd):
        control_payload = set_one_of(ControlPayloadV2, ControlInvocationV2(1, set_one_of(ControlCommandV2, cmd)))
        self._input_queue.put(ControlElement(tag=ActorVirtualIdentity("CONTROLLER"), payload=control_payload))

    def send_data(self, data_element):
        self._input_queue.put(data_element)


class Controller(threading.Thread):
    def __init__(self, workflow, input_queue: Queue):
        super().__init__()
        self._workflow = workflow
        self._input_queue = input_queue
        self._worker_status = {}
        self.initialize()
        self._running = True

    def run(self):
        while self._running:
            msg = self._input_queue.get()
            self.process(msg)

    def process(self, msg: ControlElement):
        # print(f"controller processing {msg}")
        self.process_control_payload(msg.tag, msg.payload)

    def process_control_payload(self, tag: ActorVirtualIdentity, payload: ControlPayloadV2) -> None:
        """
        Process the given ControlPayload with the tag.
        :param tag: ActorVirtualIdentity, the sender.
        :param payload: ControlPayloadV2 to be handled.
        """
        # logger.debug(f"processing one CONTROL: {payload} from {tag}")
        match(
            (tag, get_one_of(payload)),
            typing.Tuple[ActorVirtualIdentity, ControlInvocationV2], self._process_control_invocation,
            typing.Tuple[ActorVirtualIdentity, ReturnInvocationV2], self._process_control_return
        )

    def _process_control_return(self, tag, return_invocation: ReturnInvocationV2):

        # print(return_invocation.control_return)
        if return_invocation.control_return.worker_state:
            self._worker_status[tag] = return_invocation.control_return.worker_state
        elif return_invocation.control_return.worker_statistics:
            statistics = return_invocation.control_return.worker_statistics
            self._worker_status[tag] = statistics.worker_state

    def _process_control_invocation(self, tag, control_invocation: ControlInvocationV2):
        command = get_one_of(control_invocation.command)
        logger.debug(command)
        if command == WorkerExecutionCompletedV2():
            self._worker_status[tag] = "Done"
            if all(i == "Done" for i in self._worker_status.values()):
                for proxy in self._workflow.worker_proxies.values():
                    proxy.process.kill()
                    logger.debug(f"killed {proxy.id}")
                self._running = False

    def broadcast(self, cmd, target_proxies=None):
        if target_proxies is None:
            target_proxies = self._workflow.worker_proxies.values()
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
                        dst_worker_proxy = self._workflow.worker_proxies[dst_id]
                        dst_worker_proxy.send_data(msg)
                elif isinstance(msg, ControlElement):
                    msg.tag = ActorVirtualIdentity(worker_proxy.id)
                    self._input_queue.put(msg)

        for oid, operator in self._workflow.operators.items():
            worker_proxy = WorkerProxy()
            self._workflow.worker_proxies[oid] = worker_proxy
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

        for oid, worker_proxy in dict(self._workflow.worker_proxies).items():
            threading.Thread(target=message_forwarder, args=(worker_proxy,),daemon=True).start()

        for lid, link in self._workflow.links.items():
            src_op_proxy = self._workflow.worker_proxies[link.from_]
            partitioning = set_one_of(Partitioning, OneToOnePartitioning(1, [ActorVirtualIdentity(link.to)]))
            link_id = LinkIdentity(from_=LayerIdentity("", link.from_, ""), to=LayerIdentity("", link.to, ""))
            src_op_proxy.send_cmd(UpdateInputLinkingV2(ActorVirtualIdentity(link.from_), link_id))
            src_op_proxy.send_cmd(AddPartitioningV2(link_id, partitioning))

        for oid, operator in self._workflow.operators.items():
            worker_proxy = self._workflow.worker_proxies[oid]
            if operator.is_source:
                worker_proxy.send_cmd(StartWorkerV2())
            worker_proxy.send_cmd(QueryStatisticsV2())


class Workflow:
    def __init__(self):
        self.operators = dict()
        self.links = dict()
        self.worker_proxies: typing.Mapping[str, WorkerProxy] = dict()

    def add_operator(self, operator: Operator):
        oid = gen_uuid("op")
        self.operators[oid] = operator
        return oid

    def add_link(self, link: Link):
        lid = gen_uuid("link")
        self.links[lid] = link
        return lid

    def start(self):
        controller_queue = Queue()
        controller = Controller(self, controller_queue)
        controller.start()
        print("here")

    def wait(self):
        for worker_proxy in self.worker_proxies.values():
            worker_proxy.process.wait()
