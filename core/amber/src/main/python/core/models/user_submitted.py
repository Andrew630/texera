from typing import Union, Iterator, Optional

from core.models import Tuple, InputExhausted, TupleLike
from core.models.operator import metadata
from core.models.workflow import Workflow, Link
from pytexera import UDFOperator


@metadata(output_schema={'a': 'string'}, is_source=True)
class Op1(UDFOperator):

    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        import time
        print(time.time())
        yield {'a': 'hello'}


@metadata(output_schema={'a': 'string', 'b': 'integer'})
class Op2(UDFOperator):
    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        if isinstance(tuple_, Tuple):
            tuple_['b'] = 10
            yield tuple_

if __name__ == '__main__':
    wf = Workflow()
    op1 = Op1()
    op1_id = wf.add_operator(op1)
    op2 = Op2()
    op2_id = wf.add_operator(op2)
    wf.add_link(Link(op1_id, op2_id))
    wf.add_link(Link(op2_id, "CONTROLLER"))
    wf.start()
