import { Handle, Position } from '@xyflow/react';
import { NodeData } from '../../types/types';
import React from 'react';
import CloseButton from '../buttons/CloseButton';
import ExpressionInput from '../inputs/ExpressionInput';

interface IProps {
  data: NodeData;
  id: string;
}

const validatorRegex = /^(?:\s*\(?\s*\w+\s*(==|!=|<=|>=|<|>)\s*(?:\w+|\d+)\s*\)?\s*(?:(\|\||&&)\s*\(?\s*\w+\s*(==|!=|<=|>=|<|>)\s*(?:\w+|\d+)\s*\)?\s*)*)$/

// Example of a modified While Block component
const WhileBlock: React.FC<IProps> = ({ data }) => {
  return (
    <div className="px-4 py-2 shadow-md rounded-md bg-white border-2 border-cyan-500 min-w-[150px]">
      <div className="flex items-center justify-center font-bold text-lg pb-2 border-b border-gray-200">
        While Loop
      </div>

      <div className="mt-2">
        <label className="block text-sm font-medium text-gray-700">Condition:</label>
        <input
          value={data.expression || ''}
          onChange={(e) => data.setExpression?.(e.target.value)}
          className="mt-1 p-1 w-full border rounded-md"
          placeholder="Enter condition"
        />
      </div>


      <Handle
        type="target"
        position={Position.Top}
        id="in"
        className="!w-3 !h-3 bg-cyan-500"
      />

      <Handle
        type="source"
        position={Position.Right}
        id="body"
        className="!w-3 !h-3 bg-green-500 top-1/3"
      >
        <div className="absolute -top-6 left-3 px-6 py-1 bg-green-100 text-green-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap">
          Body
        </div>
      </Handle>

      <Handle
        type="source"
        position={Position.Left}
        id="next"
        className="!w-3 !h-3 bg-red-500"
      >
        <div className="absolute -top-6 right-3 px-6 py-1 bg-red-100 text-red-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap">
          Exit Loop
        </div>
      </Handle>

      <Handle
        type="target"
        position={Position.Bottom}
        id="return"
        className="!w-3 !h-3 bg-purple-500 top-1/3"
      >
        <div
          className="absolute top-3 right-3 px-6 py-1 bg-purple-100 text-purple-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap">
          End Body
        </div>
      </Handle>

      <button
        className="absolute top-0 right-0 p-1 text-red-500"
        onClick={data.deleteNode}
      >
        ×
      </button>
    </div>
  );
};

export default WhileBlock;