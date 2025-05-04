// src/components/CustomNode.tsx
import { Handle, Position, NodeProps } from '@xyflow/react';
import { Block } from '../../types/flow-chart';

// Define the data shape for the node
interface NodeData {
  label: string;
  block: Block;
}

const CustomNode = ({ data }: NodeProps<NodeData>) => {
  const { label, block } = data;
  const isCondition = block.type === 'condition';
  const isWhile = block.type === 'while';
  const isEnd = block.type === 'end' || block.type === 'end_condition';

  const nodeStyle = {
    condition: 'transform rotate-45 bg-blue-100 border-blue-500 w-32 h-32 flex items-center justify-center',
    while: 'transform rotate-45 bg-blue-100 border-blue-500 w-32 h-32 flex items-center justify-center',
    end: 'rounded-full bg-gray-100 border-gray-500 w-24 h-12 flex items-center justify-center',
    end_condition: 'rounded-full bg-gray-100 border-gray-500 w-24 h-12 flex items-center justify-center',
    default: 'rounded bg-white border-gray-500 w-32 h-16 flex items-center justify-center',
  };

  const getNodeStyle = () => {
    if (isCondition) return nodeStyle.condition;
    if (isWhile) return nodeStyle.while;
    if (isEnd) return nodeStyle[block.type];
    return nodeStyle.default;
  };

  return (
    <div className={`border ${getNodeStyle()}`}>
      {!isEnd && (
        <Handle type="target" position={Position.Top} className="w-2 h-2 bg-gray-700" />
      )}
      <div className={isCondition || isWhile ? 'transform -rotate-45' : ''}>{label}</div>
      {!isEnd && (
        <>
          <Handle
            type="source"
            position={Position.Bottom}
            id="next"
            className="w-2 h-2 bg-gray-700"
          />
          {isCondition && (
            <>
              <Handle
                type="source"
                position={Position.Right}
                id="true"
                className="w-2 h-2 bg-green-500"
              />
              <Handle
                type="source"
                position={Position.Left}
                id="false"
                className="w-2 h-2 bg-red-500"
              />
            </>
          )}
          {isWhile && (
            <Handle
              type="source"
              position={Position.Right}
              id="body"
              className="w-2 h-2 bg-blue-500"
            />
          )}
        </>
      )}
    </div>
  );
};

export const nodeTypes: Record<BlockType, React.ComponentType<any>> = {
  input: CustomNode,
  assign: CustomNode,
  print: CustomNode,
  while: CustomNode,
  condition: CustomNode,
  end_condition: CustomNode,
  end: CustomNode,
};