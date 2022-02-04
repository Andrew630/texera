import typing
from queue import Queue

from core.models import ControlElement, Operator
from core.models.controller import Controller
from core.models.link import Link
from core.models.worker_proxy import WorkerProxy
from core.util import set_one_of, gen_uuid
from proto.edu.uci.ics.amber.engine.architecture.worker import ControlCommandV2, PauseWorkerV2, ResumeWorkerV2
from proto.edu.uci.ics.amber.engine.common import ControlPayloadV2, ControlInvocationV2, ActorVirtualIdentity


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
        self.controller_queue = Queue()
        self.controller = Controller(self, self.controller_queue)
        self.controller.start()

    def wait(self):
        for worker_proxy in self.worker_proxies.values():
            worker_proxy.process.wait()

    def interact(self):

        while True:
            line = input(">")
            commands = tuple(line.split())
            if commands[0] not in self.controller.available_user_commands:
                print(f"non-recognized command {commands}, please try again")
                continue
            self.controller_queue.put(commands)
