import React from 'react';
import { Handle, Position } from '@xyflow/react';
import { validateExpression } from '../../utils/expressionValidator';
import { NodeData } from '../../types/types';
import CloseButton from '../buttons/CloseButton';
import ExpressionInput from '../inputs/ExpressionInput';
import HandleWrapper from '../wrapper/HandleWrapper';

interface IProps {
  data: NodeData;
  id: string;
}

const validatorRegex =
  /^(?:\s*\(?\s*\w+\s*(==|!=|<=|>=|<|>)\s*(?:\w+|\d+)\s*\)?\s*(?:(\|\||&&)\s*\(?\s*\w+\s*(==|!=|<=|>=|<|>)\s*(?:\w+|\d+)\s*\)?\s*)*)$/;

const ConditionBlock: React.FC<IProps> = ({ data }) => {
  return (
    <div className="bg-white border-2 border-gray-300 rounded-lg p-4 shadow-md min-w-[200px] relative">
      <CloseButton onClick={data.deleteNode} />

      <Handle type="target" position={Position.Top} className="!w-3 !h-3 bg-blue-500" />
      <div className="font-bold text-center">CONDITION</div>
      <div className="mt-2">
        <ExpressionInput
          expression={data.expression}
          setExpression={data.setExpression}
          validatorRegex={validatorRegex}
          placeholder={'e.g., X < 5 or X == 5 or X > 5'}
        />
      </div>
      <HandleWrapper
        type={'source'}
        position={Position.Left}
        id={'true'}
        label={'True'}
        isVisible={true}
        className={'!w-3 !h-3 bg-green-500'}
        labelClassName={
          'absolute -top-6 right-3 px-6 py-1 bg-green-100 text-green-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap'
        }
      />
      <HandleWrapper
        type={'source'}
        position={Position.Right}
        id={'false'}
        label={'False'}
        isVisible={true}
        className={'!w-3 !h-3 bg-red-500'}
        labelClassName={
          'absolute -top-6 left-3 px-6 py-1 bg-red-100 text-red-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap'
        }
      />
    </div>
  );
};

export default ConditionBlock;
