from typing import Union, Iterator, Optional

from pytexera import *


class WorkflowCompiler:
    pass
    # @classmethod
    # def compile(cls, ):


if __name__ == '__main__':
    class Op1(UDFOperator):

        @UDFOperator.output(schema={'a': 'string', 'b': 'integer'})
        def process_tuple(self, tuple_: Union[Tuple, InputExhausted], input_: int) -> Iterator[Optional[TupleLike]]:
            yield {'a': 'hello'}


    op = Op1()
    op.process_tuple(Tuple({'other': 1}), 1)
    print(op.output_schema)
