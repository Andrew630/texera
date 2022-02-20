from queue import Queue
from threading import Event

from loguru import logger
from overrides import overrides
from pampy import match

from core.models import Tuple, InputExhausted
from core.models.tdb import TDB, QueueIn, QueueOut
from core.util import StoppableQueueBlockingRunnable, DoubleBlockingQueue, IQueue


class DataProcessorReal(StoppableQueueBlockingRunnable):
    def __init__(self,
                 input_queue: DoubleBlockingQueue,
                 output_queue: Queue,
                 operator,
                 dp_condition,
                 async_rpc_client):
        super().__init__(self.__class__.__name__, input_queue)
        self._input_queue = input_queue
        self._output_queue = output_queue
        self._operator = operator
        self._dp_condition = dp_condition
        self._finished_current = Event()
        self._running = Event()
        self.notifiable = Event()
        self.notifiable.set()
        queue_in, queue_out = QueueIn(), QueueOut(async_rpc_client)
        self.debug_input_queue = queue_in.queue
        def channel_A():
            self.notifiable.set()
        def channel_B():
            self.notifiable.clear()
            with self._dp_condition:
                self._dp_condition.notify()
        self._tdb = TDB(queue_in, queue_out, channel_A, channel_B)


    @overrides
    def receive(self, next_entry: IQueue.QueueElement) -> None:
        """
        Main entry point of the DataProcessor. Upon receipt of an next_entry, process it respectfully.

        :param next_entry: An entry from input_queue, could be one of the followings:
                    1. a ControlElement;
                    2. a DataElement.
        """
        match(
            next_entry,
            (Tuple, int), self._process_tuple,
            (InputExhausted, int), self._process_tuple,
            str, self._process_breakpoint
        )

    def _process_tuple(self, tuple_, input_):

        self.switch_executor()
        self.check_and_process_breakpoint()
        for output in self._operator.get().process_tuple(tuple_, input_):
            self._output_queue.put(Tuple(output) if output is not None else None)

            self.switch_executor()
            self.check_and_process_breakpoint()
        self._finished_current.set()
        self.switch_executor()
        self.check_and_process_breakpoint()

    def check_and_process_breakpoint(self):
        while not self._input_queue.main_empty():
            _ = self.interruptible_get()
            self._process_breakpoint()

    def switch_executor(self):
        with self._dp_condition:
            self._dp_condition.notify()
            self._dp_condition.wait()

    def _process_breakpoint(self):
        self._tdb.set_trace()

    def pre_start(self) -> None:
        with self._dp_condition:
            self._dp_condition.wait()
