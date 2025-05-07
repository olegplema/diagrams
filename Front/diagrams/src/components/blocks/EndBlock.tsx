import { NodeData } from '../../types/types';
import React, { useState } from 'react';
import { Handle, Position, useStore } from '@xyflow/react';
import CloseButton from '../buttons/CloseButton';
import HandleWrapper from '../wrapper/HandleWrapper';

interface IProps {
  data: NodeData;
  id: string;
}

const EndBlock: React.FC<IProps> = ({ data, id }) => {
  const [isHovered, setIsHovered] = useState(false);
  const [connectedSourceHandles, setConnectedSourceHandles] = useState<string[]>([]);

  const edges = useStore(state => state.edges);
  const connectedTargetHandles = edges
    .filter(edge => edge.target === id)
    .map(edge => edge.targetHandle || '');

  const handleMouseEnter = () => setIsHovered(true);
  const handleMouseLeave = () => setIsHovered(false);

  const handleConnect = (handleId: string) => {
    setConnectedSourceHandles(prev => (prev.includes(handleId) ? prev : [...prev, handleId]));
  };

  const isHandleVisible = (handleId: string): boolean => {
    const isTarget = handleId.startsWith('target');
    const isSource = handleId.startsWith('source');

    const anyTargetConnected = connectedTargetHandles.length > 0;
    const anySourceConnected = connectedSourceHandles.length > 0;

    if (isTarget) {
      return anyTargetConnected ? connectedTargetHandles.includes(handleId) : isHovered;
    }
    if (isSource) {
      return anySourceConnected ? connectedSourceHandles.includes(handleId) : isHovered;
    }

    return false;
  };

  return (
    <div
      className="bg-white border-2 border-gray-300 rounded-lg p-4 shadow-md min-w-[200px] relative"
      onMouseEnter={handleMouseEnter}
      onMouseLeave={handleMouseLeave}
    >
      <CloseButton onClick={data.deleteNode} />
      <div className="font-bold text-center">END</div>

      <HandleWrapper
        type="target"
        position={Position.Top}
        id="target-top"
        label="Prev"
        isVisible={isHandleVisible('target-top')}
        className="!w-3 !h-3 bg-blue-500"
        labelClassName="absolute bottom-3 right-3 px-4 py-1 bg-blue-100 text-blue-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap"
      />

      <HandleWrapper
        type="target"
        position={Position.Right}
        id="target-right"
        label="Prev"
        isVisible={isHandleVisible('target-right')}
        className="!w-3 !h-3 bg-blue-500"
        labelClassName="absolute bottom-3 left-3 px-4 py-1 bg-blue-100 text-blue-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap"
      />

      <HandleWrapper
        type="source"
        position={Position.Bottom}
        id="source-bottom"
        label="Next"
        isVisible={isHandleVisible('source-bottom')}
        onConnect={() => handleConnect('source-bottom')}
        className="!w-3 !h-3 bg-green-500"
        labelClassName="absolute top-3 right-3 px-4 py-1 bg-orange-100 text-orange-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap"
      />

      <HandleWrapper
        type="source"
        position={Position.Left}
        id="source-left"
        label="Next"
        isVisible={isHandleVisible('source-left')}
        onConnect={() => handleConnect('source-left')}
        className="!w-3 !h-3 bg-green-500"
        labelClassName="absolute top-3 right-3 px-4 py-1 bg-orange-100 text-orange-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap"
      />
    </div>
  );
};

export default EndBlock;
