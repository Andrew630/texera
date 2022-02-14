import time

from loguru import logger

from proto.edu.uci.ics.amber.engine.architecture.worker import DebugCommandV2, ControlCommandV2, DebugPromptV2
from proto.edu.uci.ics.amber.engine.common import ActorVirtualIdentity
from .handler_base import Handler
from ..managers.context import Context
from ...util import set_one_of
from ...util.operator import modules


class DebugCommandHandler(Handler):
    cmd = DebugCommandV2

    def __init__(self):
        self.established = False

    def __call__(self, context: Context, command: cmd, *args, **kwargs):
        logger.info(command.cmd)
        tokens = command.cmd.split()
        debug_input_queue = context.dp.data_processor_real.debug_input_queue
        debug_output_queue = context.dp.data_processor_real.debug_output_queue
        if not self.established:
            self.establish(context)
        if tokens[0] == "b" and len(tokens) > 1:
            debug_input_queue.put(f"b {modules[0]}:{command.cmd.split()[1]}\n")
            prompt = debug_output_queue.get()
        elif tokens[0] in ['c', 'cont', 'continue', 'utl']:
            debug_input_queue.put(f"{command.cmd}\n")
            return

        else:
            debug_input_queue.put(f"{command.cmd}\n")
            prompt = debug_output_queue.get()
            logger.error(f"got prompt")

        control_command = set_one_of(ControlCommandV2, DebugPromptV2(prompt))
        context.dp._async_rpc_client.send(ActorVirtualIdentity(name="CONTROLLER"), control_command)
        logger.error(f"done handling {command}")
        return None

    def establish(self, context):
        context.dp._data_input_queue.put("here is a breakpoint!!!")
        context.dp.switch_executor(1000)
        logger.info(context.dp.data_processor_real.debug_output_queue.get())
        self.established = True
