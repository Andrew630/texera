import time
from queue import Queue
from threading import Event

from loguru import logger

from core.models import Tuple, InternalQueue
from core.models.tdb import TDB
from core.util.runnable.runnable import Runnable


class DataProcessorReal(Runnable):
    def __init__(self, main_input_queue: InternalQueue,

                 input_queue: Queue,
                 output_queue: Queue,
                 operator,
                 dp_condition,
                 set_breakpoint_event:Event,
                 hit_breakpoint_event:Event,
                 tdb_port: int):
        self._main_input_queue = main_input_queue
        self._input_queue = input_queue
        self._output_queue = output_queue
        self._operator = operator
        self._dp_condition = dp_condition
        self._finished_current = Event()
        self._running = Event()
        self.set_breakpoint_event = set_breakpoint_event
        self.hit_breakpoint_event = hit_breakpoint_event
        self._tdb_port = tdb_port

    def run(self) -> None:

        # self._tdb= TDB(self._main_input_queue, self._dp_condition, port=self._tdb_port)
        # self._tdb.set_trace()

        with self._dp_condition:
            self._dp_condition.wait()
        self._running.set()
        logger.info("starting")

        while self._running.is_set():
            self.check_breakpoint()
            self.switch_executor()

            if not self._input_queue.empty():
                logger.info("trying to get next tuple")
                tuple_, input_ = self._input_queue.get()
                # self.check_breakpoint()
                # self.switch_executor()
                # print(f"get tuple {tuple_}")
                for output in self._operator.get().process_tuple(tuple_, input_):
                    self._output_queue.put(Tuple(output) if output is not None else None)
                    # print(f"done one step of {tuple_}, notifying cp")
                    self.check_breakpoint()
                    self.switch_executor()

                self._finished_current.set()
                logger.info("finished current tuple")
                self.check_breakpoint()
                self.switch_executor()

    def check_breakpoint(self):

        if self.set_breakpoint_event.is_set():
            logger.info("trying to get bp")
            self._tdb = TDB(self._main_input_queue, self.hit_breakpoint_event, self._dp_condition, port=self._tdb_port)
            self.set_breakpoint_event.clear()
            self._tdb.set_trace()
        else:
            logger.info("bp queue empty")

    def switch_executor(self):
        with self._dp_condition:
            self._dp_condition.notify()
            self._dp_condition.wait()
            time.sleep(1)
            logger.info("back from CP")
