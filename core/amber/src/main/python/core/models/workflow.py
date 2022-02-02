import inspect
import socket
import subprocess
import threading
import time
import uuid
from contextlib import closing

from loguru import logger
from pyarrow import Schema

from core.models import InternalQueue, ControlElement, DataElement, Operator, EndOfUpstream
from core.runnables import NetworkReceiver, NetworkSender
from core.util import set_one_of
from core.util.arrow_utils import from_arrow_schema
from proto.edu.uci.ics.amber.engine.architecture.sendsemantics import Partitioning, OneToOnePartitioning
from proto.edu.uci.ics.amber.engine.architecture.worker import ControlCommandV2, InitializeOperatorLogicV2, \
    AddPartitioningV2, OpenOperatorV2, StartWorkerV2, UpdateInputLinkingV2
from proto.edu.uci.ics.amber.engine.common import ControlPayloadV2, ControlInvocationV2, ActorVirtualIdentity, \
    LinkIdentity, LayerIdentity


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
            ["python", "../../texera_run_python_worker.py", str(self.input_port), str(self.output_port), "INFO"])
        connected = False
        while not connected:
            try:
                time.sleep(1)
                logger.debug(f"trying to connect input_port={self.input_port}, output_port={self.output_port}")
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

    def send_data(self, data_element):
        self._input_queue.put(data_element)


class Workflow:
    def __init__(self):
        self.operators = dict()
        self.links = dict()
        self.worker_proxies = dict()

    def add_operator(self, operator: Operator):
        oid = gen_uuid("op")
        self.operators[oid] = operator
        return oid

    def add_link(self, link: Link):
        lid = gen_uuid("link")
        self.links[lid] = link
        return lid

    def start(self):

        def message_forwarder():
            while True:
                for oid, worker_proxy in dict(self.worker_proxies).items():
                    msg = worker_proxy._output_queue.get()
                    if isinstance(msg, DataElement):
                        vid = msg.tag
                        dst_id = vid.name
                        if dst_id != "CONTROLLER":
                            dst_worker_proxy = self.worker_proxies[dst_id]
                            dst_worker_proxy.send_data(msg)
                        if not isinstance(msg.payload, EndOfUpstream):
                            print(msg.payload.frame.to_pydict())

        threading.Thread(target=message_forwarder).start()

        for oid, operator in self.operators.items():
            worker_proxy = WorkerProxy()
            self.worker_proxies[oid] = worker_proxy
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

        for lid, link in self.links.items():
            src_op_proxy = self.worker_proxies[link.from_]
            partitioning = set_one_of(Partitioning, OneToOnePartitioning(1, [ActorVirtualIdentity(link.to)]))
            link_id = LinkIdentity(from_=LayerIdentity("", link.from_, ""), to=LayerIdentity("", link.to, ""))
            src_op_proxy.send_cmd(UpdateInputLinkingV2(ActorVirtualIdentity(link.from_), link_id))
            src_op_proxy.send_cmd(AddPartitioningV2(link_id, partitioning))

        for oid, operator in self.operators.items():
            worker_proxy = self.worker_proxies[oid]
            if operator.is_source:
                worker_proxy.send_cmd(StartWorkerV2())

    def wait(self):
        for worker_proxy in self.worker_proxies.values():
            worker_proxy.process.wait()
