from core.models import Operator, Link
from core.util import gen_uuid


class Workflow:
    def __init__(self):
        self.operators = dict()
        self.links = dict()

    def add_operator(self, operator: Operator):
        oid = gen_uuid("op")
        self.operators[oid] = operator
        return oid

    def add_link(self, link: Link):
        lid = gen_uuid("link")
        self.links[lid] = link
        return lid
