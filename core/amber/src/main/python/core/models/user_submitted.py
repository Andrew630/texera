from typing import Union, Iterator, Optional

from core.models import Tuple, InputExhausted, TupleLike
from core.models.workflow import Workflow, Link
from pytexera import UDFOperator


class Op1(UDFOperator):

    @UDFOperator.output(schema={'a': 'string'})
    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        import time
        print(time.time())
        yield {'a': 'hello'}


class Op2(UDFOperator):

    @UDFOperator.output(schema={'a': 'string', 'b': 'integer'})
    def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
        if isinstance(tuple_, Tuple):
            tuple_['b'] = 10
            yield tuple_


if __name__ == '__main__':
    wf = Workflow()
    op1 = Op1()
    op1.is_source = True
    op1_id = wf.add_operator(op1)
    op2 = Op2()
    op2.is_source = False
    op2_id = wf.add_operator(op2)
    wf.add_link(Link(op1_id, op2_id))
    wf.add_link(Link(op2_id, "CONTROLLER"))
    wf.start()
    
