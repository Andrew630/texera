from typing import Union, Iterator, Optional

from loguru import logger

from core.models import Tuple, InputExhausted, TupleLike
from core.models.operator import metadata
from core.models.workflow import Workflow, Link
from pytexera import UDFOperator

logger.remove()
logger.add(open("python.log", "w+"), level="DEBUG")


@metadata(output_schema={'time': 'timestamp', 'id': 'integer'}, is_source=True)
class Op1(UDFOperator):
    def open(self):
        self.cap = 200

    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        import time
        i = 0
        while i < self.cap:
            i += 1
            time.sleep(0.1)
            from datetime import datetime
            now = datetime.now()
            yield {'time': now, 'id': i}


@metadata(output_schema={'time': 'timestamp', 'id': 'integer'})
class Op2(UDFOperator):
    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        if isinstance(tuple_, Tuple):
            tuple_['b'] = 10
            yield tuple_


@metadata(is_sink=True)
class Op3(UDFOperator):
    def open(self):
        import csv
        self.out_file = open("out.csv", 'w+')
        self.writer = csv.writer(self.out_file)

    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        if isinstance(tuple_, Tuple):
            self.writer.writerow(tuple_.get_fields())
            self.out_file.flush()
            yield

    def close(self):
        self.out_file.close()


if __name__ == '__main__':
    wf = Workflow()
    op1 = Op1()
    op1_id = wf.add_operator(op1)
    op2 = Op2()
    op2_id = wf.add_operator(op2)
    op3 = Op3()
    op3_id = wf.add_operator(op3)
    wf.add_link(Link(op1_id, op2_id))
    wf.add_link(Link(op2_id, op3_id))
    wf.add_link(Link(op3_id, "CONTROLLER"))
    wf.start()
    wf.interact()
