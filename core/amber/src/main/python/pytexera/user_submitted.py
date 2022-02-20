from typing import Union, Iterator, Optional

from core.models import Tuple, InputExhausted, TupleLike, metadata, Workflow, Link
from pytexera import UDFOperator
from pytexera.workflow_driver import WorkflowDriver


@metadata(output_schema={'time': 'timestamp', 'id': 'integer'}, is_source=True)
class Op1(UDFOperator):
    def open(self):
        import time
        print(time.time_ns())
        self.cap = 100000

    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        i = 0
        while i < self.cap:
            i += 1
            yield
            import time
            time.sleep(5)
            from datetime import datetime
            now = datetime.now()
            yield {'time': now, 'id': i}


@metadata(output_schema={'time': 'timestamp', 'id': 'integer'})
class Op2(UDFOperator):
    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        if isinstance(tuple_, Tuple):
            tuple_['b'] = 10
            yield
            yield tuple_


@metadata(is_sink=True)
class Op3(UDFOperator):
    def open(self):
        import csv
        self.out_file = open("../../../../../log/out.csv", 'w+')
        self.writer = csv.writer(self.out_file)

    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        if isinstance(tuple_, Tuple):
            import time
            start = time.time_ns()/1000
            s = 0
            for i in range(1000):
                s += i
            tuple_['processing_time'] = time.time_ns()/1000 - start
            self.writer.writerow(tuple_.get_fields())
            self.out_file.flush()
            yield

    def close(self):
        self.out_file.close()
        import time
        print(time.time_ns())

    def unused_function(self):
        print("this is not being used")


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
