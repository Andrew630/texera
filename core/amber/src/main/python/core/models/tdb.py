import sys
from queue import Queue
from threading import Condition, Event
from types import FrameType
from typing import Optional

import rpdb as rpdb
from loguru import logger

from core.models import InternalQueue


class TDB(rpdb.Rpdb):
    DEFAULT_ADDR = "127.0.0.1"
    DEFAULT_PORT = 4444

    def __init__(self, input_queue: InternalQueue, hit_breakpoint_event: Event, condition: Condition, addr=DEFAULT_ADDR,
                 port=DEFAULT_PORT):
        self._input_queue = input_queue
        self._hit_breakpoint_event = hit_breakpoint_event
        self._condition = condition
        with self._condition:
            self._condition.notify()
        super().__init__(addr, port)

    def user_line(self, frame: FrameType) -> None:
        try:
            self._input_queue.disable_sub()
        except:
            pass
        self._hit_breakpoint_event.set()
        logger.info("set hit breakpoint")

        with self._condition:
            logger.info("DP is trying to notify CP")
            self._condition.notify()
        logger.info("triggered !!!!!!!!!!!!")
        super(TDB, self).user_line(frame)

    def do_continue(self, arg: str) -> Optional[bool]:
        try:
            self._input_queue.enable_sub()
        except:
            pass
        return super(TDB, self).do_continue(arg)

    do_c = do_cont = do_continue

    def do_next(self, arg: str) -> Optional[bool]:
        try:
            self._input_queue.enable_sub()
        except:
            pass
        return super(TDB, self).do_next(arg)

    do_n = do_next


if __name__ == '__main__':
    tdb = TDB(Queue(), Event(), Condition())
    rpdb.handle_trap("0.0.0.0", 4444)
    tdb.set_trace()
    while True:
        import time

        time.sleep(5)
        print(time.time_ns())
