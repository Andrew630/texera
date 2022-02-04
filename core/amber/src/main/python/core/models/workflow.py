import typing

from core.models import Operator
from core.models.link import Link
from core.util import gen_uuid
from pyamber.worker_proxy import WorkerProxy


class Workflow:
    def __init__(self):
        self.operators = dict()
        self.links = dict()
        self.worker_proxies: typing.Mapping[str, WorkerProxy] = dict()

    def add_operator(self, operator: Operator):
        oid = gen_uuid("op")
        self.operators[oid] = operator
        return oid

    def add_link(self, link: Link):
        lid = gen_uuid("link")
        self.links[lid] = link
        return lid
