from queue import Queue
from threading import Thread

from loguru import logger

from core.models import Workflow, DataElement
from core.util import DoubleBlockingQueue
from pyamber.mock_controller import Controller


class WorkflowDriver:
    def __init__(self, workflow: Workflow, log_path="../../../../../log/python.log"):
        self.controller_queue = DoubleBlockingQueue(DataElement)
        self._workflow = workflow
        self.controller = None
        logger.remove()
        logger.add(open(log_path, "w+"), level="ERROR")

    def start(self):

        self.controller = Controller(self._workflow, self.controller_queue)
        self.controller_thread = Thread(target = self.controller.run)
        self.controller_thread.start()

    def interact(self):
        while True:
            line = input(">")
            commands = tuple(line.strip().split())
            if not commands:
                continue
            if commands[0] == "quit":
                return
            elif commands[0] not in self.controller.available_user_commands:
                print(f"non-recognized command {commands}, please try again")
                continue

            self.controller_queue.put(commands)
