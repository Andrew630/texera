import uuid

from .proto import *
from .customized_queue import *
from .stoppable import *
from pathlib import Path


def gen_uuid(prefix=""):
    return f"{prefix}{'-' if prefix else ''}{uuid.uuid4().hex}"



def get_root() -> Path:
    """
    hardcorded to src/main/python for now.
    :return:
    """
    path = Path(__file__)
    return path.parent.parent
