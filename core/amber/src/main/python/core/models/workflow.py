import socket
import subprocess
import threading
import time
import uuid
from contextlib import closing

from core.models import InternalQueue, ControlElement
from core.runnables import NetworkReceiver, NetworkSender
from core.util import set_one_of
from proto.edu.uci.ics.amber.engine.architecture.worker import EchoV2
from proto.edu.uci.ics.amber.engine.common import ControlPayloadV2, ControlInvocationV2


def find_free_port():
    with closing(socket.socket(socket.AF_INET, socket.SOCK_STREAM)) as s:
        s.bind(('', 0))
        s.setsockopt(socket.SOL_SOCKET, socket.SO_REUSEADDR, 1)
        return s.getsockname()[1]


def gen_uuid(prefix=""):
    return prefix + str(uuid.uuid1())


class ExecutionManager:
    def __init__(self):
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
                self.network_receiver = NetworkReceiver(self._output_queue, host="0.0.0.0", port=self.output_port,
                                                        schema_map={})
                self.network_sender = NetworkSender(self._input_queue, host="0.0.0.0", port=self.input_port,
                                                    schema_map={})
                connected = True
            except:
                pass

    def send_cmd(self, cmd):
        self._input_queue.put(cmd)


class Workflow:
    def __init__(self):
        self.operators = dict()
        self.links = dict()

    def add_operator(self, operator):
        self.operators[gen_uuid("op")] = operator

    def add_link(self, link):
        self.links[gen_uuid("link")] = link

    def exec(self):
        self.execution_managers = []
        for oid, operator in self.operators.items():
            self.execution_managers.append(ExecutionManager())
        for execution_manager in self.execution_managers:
            execution_manager.process.wait()


if __name__ == '__main__':
    workflow = Workflow()
    workflow.add_operator("op1")
    th = threading.Thread(target=workflow.exec)
    th.start()

    time.sleep(2)
    control_payload = set_one_of(ControlPayloadV2, ControlInvocationV2(1, EchoV2("hello")))

    workflow.execution_managers[0].send_cmd(ControlElement(tag="1", payload=control_payload))
    time.sleep(2)
    print(workflow.execution_managers[0]._output_queue.get())
    th.join()
