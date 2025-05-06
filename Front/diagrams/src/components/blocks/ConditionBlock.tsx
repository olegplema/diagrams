import React from 'react';
import { Handle, Position } from '@xyflow/react';
import { validateExpression } from '../../utils/expressionValidator';
import { NodeData } from '../../types/types';
import CloseButton from '../buttons/CloseButton';
import ExpressionInput from '../inputs/ExpressionInput';

interface IProps {
  data: NodeData;
  id: string;
}

const validatorRegex = /^(?:\s*\(?\s*\w+\s*(==|!=|<=|>=|<|>)\s*(?:\w+|\d+)\s*\)?\s*(?:(\|\||&&)\s*\(?\s*\w+\s*(==|!=|<=|>=|<|>)\s*(?:\w+|\d+)\s*\)?\s*)*)$/

const ConditionBlock: React.FC<IProps> = ({ data }) => {
  return (
    <div className="bg-white border-2 border-gray-300 rounded-lg p-4 shadow-md min-w-[200px] relative">
      <CloseButton onClick={data.deleteNode} />

      <Handle type="target" position={Position.Top} className="w-[18px] h-[18px] bg-blue-500" />
      <div className="font-bold text-center">CONDITION</div>
      <div className="mt-2">
        <ExpressionInput
          expression={data.expression}
          setExpression={data.setExpression}
          validatorRegex={validatorRegex}
          placeholder={"e.g., X < 5 or X == 5 or X > 5"}
        />
      </div>
      <div className="relative">
        <Handle type="source" position={Position.Left} id="true" className="w-[18px] h-[18px] bg-green-500" />
        <span className="absolute left-[-40px] top-[-6px] text-xs font-bold text-green-700">true</span>
      </div>
      <div className="relative">
        <Handle type="source" position={Position.Right} id="false" className="w-[18px] h-[18px] bg-red-500" />
        <span className="absolute right-[-40px] top-[-6px] text-xs font-bold text-red-700">false</span>
      </div>
    </div>
  );
};

export default ConditionBlock;