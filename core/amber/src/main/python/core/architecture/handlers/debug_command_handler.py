import time

from loguru import logger

from proto.edu.uci.ics.amber.engine.architecture.worker import DebugCommandV2
from .handler_base import Handler
from ..managers.context import Context
from ...models.tdb import TDB
from ...util.operator import modules


class DebugCommandHandler(Handler):
    cmd = DebugCommandV2

    def __init__(self):
        self.established = False

    def __call__(self, context: Context, command: cmd, *args, **kwargs):
        logger.info(command.cmd)
        tokens = command.cmd.split()
        if tokens[0] == "b" and len(tokens) > 1:
            # "b lineno"
            if not self.established:
                self.establish(context)
            context.dp.clientSocket.send((f"b {modules[0]}:{command.cmd.split()[1]}\n").encode('utf-8'))
            logger.info(context.dp.clientSocket.recv(1024).decode('utf-8'))
        else:
            context.dp.clientSocket.send((f"{command.cmd}\n").encode('utf-8'))
            logger.info(context.dp.clientSocket.recv(1024).decode('utf-8'))
        logger.info(f"done handling {command}")
        return None

    def establish(self, context):
        context.dp._set_breakpoint_event.set()
        context.dp.switch_executor()
        time.sleep(1)
        context.dp.clientSocket.connect((TDB.DEFAULT_ADDR, context.dp._tdb_port))
        context.dp.clientSocket.recv(1024).decode('utf-8')
        self.established = True
