from typing import Union, Iterator, Optional

from core.models import Tuple, InputExhausted, TupleLike
from core.models.workflow import Workflow
from pytexera import UDFOperator


class Op1(UDFOperator):

    @UDFOperator.output(schema={'a': 'string'})
    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        import time
        print(time.time())
        yield {'a': 'hello'}


if __name__ == '__main__':
    wf = Workflow()
    op1 = Op1()
    op1.is_source = True
    wf.add_operator(op1)
    print(wf.operators)
    wf.start()
    wf.wait()
