import sys
from threading import Condition
from types import FrameType
from typing import Optional

import rpdb as rpdb

from core.models import InternalQueue

DEFAULT_ADDR = "127.0.0.1"
DEFAULT_PORT = 4444

class TDB(rpdb.Rpdb):
    def __init__(self, input_queue:InternalQueue, condition: Condition, addr=DEFAULT_ADDR, port=DEFAULT_PORT):
        input_queue.disable_sub()
        print("pdb: start pdb!", file=sys.__stderr__)
        input_queue.put("start pdb!")
        self._input_queue = input_queue
        self._condition = condition
        with self._condition:
            print("pdb: notify cp", file=sys.__stderr__)
            self._condition.notify()
        super().__init__(addr, port)

    def user_line(self, frame: FrameType) -> None:
        self._input_queue.disable_sub()
        self._input_queue.put("bp!!")
        # print("pdb: hit bp!!", file=sys.__stderr__)
        with self._condition:
            # print("pdb: notify cp", file=sys.__stderr__)
            self._condition.notify()
        super(TDB, self).user_line(frame)

    def do_continue(self, arg: str) -> Optional[bool]:
        self._input_queue.enable_sub()
        return super(TDB, self).do_continue(arg)

    do_c = do_cont = do_continue

    def do_next(self, arg: str) -> Optional[bool]:
        self._input_queue.enable_sub()
        return super(TDB, self).do_next(arg)

    do_n = do_next