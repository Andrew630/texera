import uuid

from .proto import *
from .customized_queue import *
from .stoppable import *


def gen_uuid(prefix=""):
    return f"{prefix}{'-' if prefix else ''}{uuid.uuid4().hex}"
