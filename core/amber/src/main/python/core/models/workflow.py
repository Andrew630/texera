from core.models import Operator, Link
from core.util import gen_uuid, gen_id


class Workflow:
    def __init__(self):
        self.operators = dict()
        self.links = dict()

    def add_operator(self, operator: Operator):
        oid = gen_id("op")
        self.operators[oid] = operator
        return oid

    def add_link(self, link: Link):
        lid = gen_id("link")
        self.links[lid] = link
        return lid
