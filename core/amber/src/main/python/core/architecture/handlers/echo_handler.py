from proto.edu.uci.ics.amber.engine.architecture.worker import EchoV2, EchoResponse
from .handler_base import Handler
from ..managers.context import Context


class EchoHandler(Handler):
    cmd = EchoV2

    def __call__(self, context: Context, command: EchoV2, *args, **kwargs):
        return EchoResponse(command.msg)
