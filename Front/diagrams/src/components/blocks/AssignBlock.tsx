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

const validatorRegex = /^\w+\s*=\s*(\w+|\d+|"[^"]*")$/;

const AssignBlock: React.FC<IProps> = ({ data }) => {
  return (
    <div className="bg-white border-2 border-gray-300 rounded-lg p-4 shadow-md min-w-[200px] relative">
      <CloseButton onClick={data.deleteNode} />
      <Handle type="target" position={Position.Top} className="w-[18px] h-[18px] bg-blue-500" />
      <div className="font-bold text-center">ASSIGN</div>
      <div className="mt-2">
        <ExpressionInput
          expression={data.expression}
          setExpression={data.setExpression}
          validatorRegex={validatorRegex}
          placeholder={"e.g., X = 5 or X = Y"}
        />
      </div>
      <Handle type="source" position={Position.Bottom} id="next" className="w-[18px] h-[18px] bg-blue-500" />
    </div>
  );
};

export default AssignBlock;