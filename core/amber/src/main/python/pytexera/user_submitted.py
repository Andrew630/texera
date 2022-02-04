from typing import Union, Iterator, Optional

from core.models import Tuple, InputExhausted, TupleLike, metadata, Workflow, Link
from pytexera import UDFOperator
from pytexera.workflow_driver import WorkflowDriver


@metadata(output_schema={'time': 'timestamp', 'id': 'integer'}, is_source=True)
class Op1(UDFOperator):
    def open(self):
        self.cap = 30

    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        import time
        i = 0
        while i < self.cap:
            i += 1
            yield
            time.sleep(0.1)
            yield
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
        self.out_file = open("../../../../../log/out.csv", 'w+')
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
    op1_id = wf.add_operator(Op1())
    op2_id = wf.add_operator(Op2())
    op3_id = wf.add_operator(Op3())
    wf.add_link(Link(op1_id, op2_id))
    wf.add_link(Link(op2_id, op3_id))
    wf.add_link(Link(op3_id, "CONTROLLER"))

    workflow_driver = WorkflowDriver(wf)
    workflow_driver.start()
    workflow_driver.interact()
