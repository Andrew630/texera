from loguru import logger

from core.architecture.handlers.handler_base import Handler
from core.architecture.managers.context import Context

from proto.edu.uci.ics.amber.engine.architecture.worker import ResumeWorkerV2


class ResumeWorkerHandler(Handler):
    cmd = ResumeWorkerV2

    def __call__(self, context: Context, command: ResumeWorkerV2, *args, **kwargs):

        try:
            logger.info("resume debugger")
            context.dp.clientSocket.send((f"unt\n").encode('utf-8'))
        except:
            logger.info("no debugger connected")
        context.dp._resume()
        state = context.state_manager.get_current_state()
        return state
