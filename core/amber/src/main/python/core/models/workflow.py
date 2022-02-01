import socket
import subprocess
import threading
import time
import uuid
from contextlib import closing

from core.models import InternalQueue, ControlElement, DataElement
from core.runnables import NetworkReceiver, NetworkSender
from core.util import set_one_of
from proto.edu.uci.ics.amber.engine.architecture.sendsemantics import Partitioning, OneToOnePartitioning
from proto.edu.uci.ics.amber.engine.architecture.worker import ControlCommandV2, InitializeOperatorLogicV2, \
    AddPartitioningV2, OpenOperatorV2, StartWorkerV2
from proto.edu.uci.ics.amber.engine.common import ControlPayloadV2, ControlInvocationV2, ActorVirtualIdentity, \
    LinkIdentity, LayerIdentity


def find_free_port():
    with closing(socket.socket(socket.AF_INET, socket.SOCK_STREAM)) as s:
        s.bind(('', 0))
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        return s.getsockname()[1]


def gen_uuid(prefix=""):
    return prefix + str(uuid.uuid1())


class WorkerProxy:
    def __init__(self):
        self.id = gen_uuid("Worker")
        self.input_port = find_free_port()
        self.output_port = find_free_port()
        self._input_queue = InternalQueue()
        self._output_queue = InternalQueue()
        self.process = subprocess.Popen(
            ["python", "../../texera_run_python_worker.py", str(self.input_port), str(self.output_port), "INFO"])
        connected = False
        while not connected:
            try:
                time.sleep(1)
                print(f"trying to connect input_port={self.input_port}, output_port={self.output_port}")
                self.network_receiver = NetworkReceiver(self._output_queue, host="0.0.0.0", port=self.output_port)
                threading.Thread(target=self.network_receiver.run).start()
                self.network_sender = NetworkSender(self._input_queue, host="0.0.0.0", port=self.input_port)
                threading.Thread(target=self.network_sender.run).start()
                connected = True
            except Exception as err:
                print(err)

    def send_cmd(self, cmd):
        control_payload = set_one_of(ControlPayloadV2, ControlInvocationV2(1, set_one_of(ControlCommandV2, cmd)))
        self._input_queue.put(ControlElement(tag=ActorVirtualIdentity("CONTROLLER"), payload=control_payload))

    def send_data(self, data, schema):
        batch = data
        batch.schema = schema
        self._output_queue.put(DataElement(tag=ActorVirtualIdentity(self.id), payload=batch))


class Workflow:
    def __init__(self):
        self.operators = dict()
        self.links = dict()

    def add_operator(self, operator):
        self.operators[gen_uuid("op")] = operator

    def add_link(self, link):
        self.links[gen_uuid("link")] = link

    def exec(self):
        self.worker_proxies = []
        for oid, operator in self.operators.items():
            self.worker_proxies.append(WorkerProxy())
        for worker_proxy in self.worker_proxies:
            worker_proxy.process.wait()


if __name__ == '__main__':
    workflow = Workflow()
    workflow.add_operator("op1")
    controller_thread = threading.Thread(target=workflow.exec)
    controller_thread.start()
    time.sleep(2)

    # should have started worker
    target_worker_proxy = workflow.worker_proxies[0]


    def f():
        while True:
            print(target_worker_proxy._output_queue.get())


    threading.Thread(target=f).start()

    workflow.worker_proxies[0].send_cmd(InitializeOperatorLogicV2(code="""
from typing import Iterator, Optional, Union
from pytexera import *

class ProcessTupleOperator(UDFOperator):
    
    def open(self):
        import time
        time.sleep(2)
        
    @overrides
    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        # if isinstance(tuple_, Tuple):
        yield {"a":"this"}
        yield {"a":"this"}
        yield {"a":"this"}
    """, is_source=True, output_schema={"a": "string"}))
    partitioning = set_one_of(Partitioning, OneToOnePartitioning(1, [ActorVirtualIdentity("op2")]))
    target_worker_proxy.send_cmd(
        AddPartitioningV2(LinkIdentity(from_=LayerIdentity(), to=LayerIdentity()), partitioning))
    target_worker_proxy.send_cmd(OpenOperatorV2())
    target_worker_proxy.send_cmd(StartWorkerV2())

    controller_thread.join()
