import bdb
import sys
from pdb import Pdb
from threading import Condition, Event
from types import FrameType

from loguru import logger

from core.models.adb import QueueIn, QueueOut


class TDB(Pdb):

    def __init__(self,
                 stdin: QueueIn,
                 stdout: QueueOut,
                 notifiable: Event,
                 condition: Condition
                 ):
        self.notifiable: Event = notifiable
        self._condition = condition
        # Backup stdin and stdout before replacing them by the socket handle
        self.old_stdout = sys.stdout
        self.old_stdin = sys.stdin
        super().__init__(stdin=stdin, stdout=stdout)
    def do_until(self, arg):
        """unt(il) [lineno]
        Without argument, continue execution until the line with a
        number greater than the current one is reached.  With a line
        number, continue execution until a line with a number greater
        or equal to that is reached.  In both cases, also stop when
        the current frame returns.
        """
        self.notifiable.set()
        return super(TDB, self).do_until(arg)
    do_unt = do_until

    def do_step(self, arg):
        """s(tep)
        Execute the current line, stop at the first possible occasion
        (either in a function that is called or in the current
        function).
        """
        self.notifiable.set()
        return super(TDB, self).do_step(arg)
    do_s = do_step

    def do_next(self, arg):
        """n(ext)
        Continue execution until the next line in the current function
        is reached or it returns.
        """
        self.notifiable.set()
        return super(TDB, self).do_next(arg)
    do_n = do_next

    def do_return(self, arg):
        """r(eturn)
        Continue execution until the current function returns.
        """
        logger.info("set notifiable to True")
        self.notifiable.set()
        return super(TDB, self).do_return(arg)
    do_r = do_return

    def do_continue(self, arg):
        """c(ont(inue))
        Continue execution, only stop when a breakpoint is encountered.
        """

        logger.error("set notifiable to True")
        self.notifiable.set()
        logger.error("do continue")
        return super(TDB, self).do_continue(arg)
    do_c = do_cont = do_continue


    def user_call(self, frame, argument_list):
        """This method is called when there is the remote possibility
        that we ever need to stop in this function."""
        logger.info("change to not notifiable")
        self.notifiable.clear()
        with self._condition:
            logger.info("DP is trying to notify CP")
            self._condition.notify()
        logger.info("triggered !!!!!!!!!!!!")
        super(TDB, self).user_call(frame, argument_list)

    def user_line(self, frame: FrameType) -> None:
        logger.error("change to not notifiable")
        self.notifiable.clear()
        with self._condition:
            logger.error("DP is trying to notify CP")
            self._condition.notify()
        logger.error("triggered !!!!!!!!!!!!")
        super(TDB, self).user_line(frame)

    def user_return(self, frame, return_value):
        """This function is called when a return trap is set here."""
        logger.info("change to not notifiable")
        self.notifiable.clear()
        with self._condition:
            logger.info("DP is trying to notify CP")
            self._condition.notify()
        logger.info("triggered !!!!!!!!!!!!")
        super(TDB, self).user_return(frame, return_value)

    def user_exception(self, frame, exc_info):
        """This function is called when a return trap is set here."""
        logger.info("change to not notifiable")
        self.notifiable.clear()
        with self._condition:
            logger.info("DP is trying to notify CP")
            self._condition.notify()
        logger.info("triggered !!!!!!!!!!!!")
        super(TDB, self).user_exception(frame, exc_info)

    def do_clear(self, arg):
        """cl(ear) filename:lineno\ncl(ear) [bpnumber [bpnumber...]]
        With a space separated list of breakpoint numbers, clear
        those breakpoints.  Without argument, clear all breaks;
        With a filename:lineno argument, clear all breaks at that
        line in that file.
        """
        if not arg:

            bplist = [bp for bp in bdb.Breakpoint.bpbynumber if bp]
            self.clear_all_breaks()
            for bp in bplist:
                self.message('Deleted %s' % bp)
            return
        if ':' in arg:
            # Make sure it works for "clear C:\foo\bar.py:12"
            i = arg.rfind(':')
            filename = arg[:i]
            arg = arg[i + 1:]
            try:
                lineno = int(arg)
            except ValueError:
                err = "Invalid line number (%s)" % arg
            else:
                bplist = self.get_breaks(filename, lineno)[:]
                err = self.clear_break(filename, lineno)
            if err:
                self.error(err)
            else:
                for bp in bplist:
                    self.message('Deleted %s' % bp)
            return
        numberlist = arg.split()
        for i in numberlist:
            try:
                bp = self.get_bpbynumber(i)
            except ValueError as err:
                self.error(err)
            else:
                self.clear_bpbynumber(i)
                self.message('Deleted %s' % bp)

    do_cl = do_clear  # 'c' is already an abbreviation for 'continue'
