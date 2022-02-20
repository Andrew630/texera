from loguru import logger

from proto.edu.uci.ics.amber.engine.architecture.worker import DebugCommandV2
from .handler_base import Handler
from ..managers.context import Context
from ...util.operator import modules


class DebugCommandHandler(Handler):
    cmd = DebugCommandV2

    def __call__(self, context: Context, command: cmd, *args, **kwargs):
        logger.error(command.cmd)
        tokens = command.cmd.split()
        debug_input_queue = context.dp.data_processor_real.debug_input_queue
        old_notifiable = context.dp.data_processor_real.notifiable.is_set()
        if context.dp.data_processor_real.notifiable.is_set():
            context.dp._pause()

        if tokens[0] == 'c':
            old_notifiable = True
        elif tokens[0] == "b" and len(tokens) > 1:
            logger.error(f"sending command to pdb [b {modules[0]}:{command.cmd.split()[1]}]")
            debug_input_queue.put(f"b {modules[0]}:{command.cmd.split()[1]}\n")
        else:
            logger.error(f"sending command to pdb [{command.cmd}]")
            debug_input_queue.put(f"{command.cmd}\n")
        if old_notifiable:
            context.dp._resume()

        logger.error(f"done handling {command.cmd}")
        return None
