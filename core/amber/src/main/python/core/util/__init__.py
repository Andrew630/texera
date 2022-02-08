import uuid
from collections import defaultdict

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


id_map = defaultdict(int)


def gen_id(prefix="") -> str:
    """
    generate sequential id per prefix. not thread-safe
    :param prefix:
    :return:
    """
    id_ = f"{prefix}-{id_map[prefix]}"
    id_map[prefix] += 1
    return id_
