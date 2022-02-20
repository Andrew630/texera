from loguru import logger

from proto.edu.uci.ics.amber.engine.architecture.worker import DebugCommandV2
from .handler_base import Handler
from ..managers.context import Context
from ...models.tdb import Tdb
from ...util.operator import modules


class DebugCommandHandler(Handler):
    cmd = DebugCommandV2

    def __call__(self, context: Context, command: str, *args, **kwargs):
        debug_input_queue = context.dp.data_processor_real.debug_input_queue
        old_notifiable = context.dp.data_processor_real.notifiable.is_set()

        if command in Tdb.resume_commands:
            context.dp._resume(mode=command)
            return

        # pause the execution, switch to communication with pdb
        if context.dp.data_processor_real.notifiable.is_set():
            context.dp._pause()

        # re-format the command with the context
        formatted_command = f"{command} {' '.join(args)}"
        if command in ["b", "break"] and len(args) > 0:
            formatted_command = f"b {modules[0]}:{args[0]}"

        # send to pdb
        logger.debug(f"sending command to pdb: {formatted_command}")
        debug_input_queue.put(f"{formatted_command}\n")

        # reverse back to the original communication channel
        if old_notifiable:
            context.dp._resume()

        return None
