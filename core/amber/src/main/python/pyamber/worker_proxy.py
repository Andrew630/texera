import subprocess
import threading
import time

from loguru import logger
from pyarrow.util import find_free_port

from core.models import InternalQueue, ControlElement
from core.runnables import NetworkReceiver, NetworkSender
from core.util import set_one_of, gen_id
from proto.edu.uci.ics.amber.engine.architecture.worker import ControlCommandV2
from proto.edu.uci.ics.amber.engine.common import ControlPayloadV2, ControlInvocationV2, ActorVirtualIdentity


class WorkerProxy:
    def __init__(self, oid):
        self.id = gen_id(oid)
        self.input_port = find_free_port()
        self.output_port = find_free_port()
        self._input_queue = InternalQueue()
        self._output_queue = InternalQueue()

        self.process = subprocess.Popen(
            ["python", "../texera_run_python_worker.py", str(self.input_port), str(self.output_port), "ERROR" if self.id =="op-2-0" else "ERROR"])
        connected = False
        while not connected:
            try:
                time.sleep(0.5)
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
