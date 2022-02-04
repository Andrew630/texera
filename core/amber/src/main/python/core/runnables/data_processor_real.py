from queue import Queue
from threading import Event

from core.models import Tuple
from core.util.runnable.runnable import Runnable


class DataProcessorReal(Runnable):
    def __init__(self, input_queue: Queue, output_queue: Queue, operator, dp_condition):
        self._input_queue = input_queue
        self._output_queue = output_queue
        self._operator = operator
        self._dp_condition = dp_condition
        self._finished_current = Event()
        self._running = Event()

    def run(self) -> None:
        with self._dp_condition:
            self._dp_condition.wait()
        self._running.set()
        # print("starting")
        while self._running.is_set():
            tuple_, input_ = self._input_queue.get()
            # print(f"get tuple {tuple_}")
            for output in self._operator.get().process_tuple(tuple_, input_):
                self._output_queue.put(Tuple(output) if output is not None else None)
                # print(f"done one step of {tuple_}, notifying cp")
                with self._dp_condition:
                    self._dp_condition.notify()
                    self._dp_condition.wait()

            self._finished_current.set()
            with self._dp_condition:
                self._dp_condition.notify()
                self._dp_condition.wait()




