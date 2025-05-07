import { NodeData } from '../../types/types';
import React, { useState } from 'react';
import { Handle, Position, useStore } from '@xyflow/react';
import CloseButton from '../buttons/CloseButton';

interface IProps {
  data: NodeData;
  id: string;
}

const EndBlock: React.FC<IProps> = ({ data, id }) => {
  const [isHovered, setIsHovered] = useState(false);
  const [connectedSourceHandles, setConnectedSourceHandles] = useState<string[]>([]);

  const edges = useStore((state) => state.edges);
  const connectedTargetHandles = edges
    .filter(edge => edge.target === id)
    .map(edge => edge.targetHandle || '');

  const handleMouseEnter = () => setIsHovered(true);
  const handleMouseLeave = () => setIsHovered(false);

  const handleConnect = (handleId: string) => {
    setConnectedSourceHandles((prev) => [...prev, handleId]);
  };

  const isHandleVisible = (handleId: string): boolean => {
    const isTarget = handleId.startsWith('target');
    const isSource = handleId.startsWith('source');

    const anyTargetConnected = connectedTargetHandles.length > 0;
    const anySourceConnected = connectedSourceHandles.length > 0;

    if (isTarget) {
      if (anyTargetConnected) {
        return connectedTargetHandles.includes(handleId);
      }
      return isHovered;
    }

    if (isSource) {
      if (anySourceConnected) {
        return connectedSourceHandles.includes(handleId);
      }
      return isHovered;
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

      {/* Incoming (Prev) */}
      <HandleWrapper
        type={'target'}
        position={Position.Top}
        id={'target-top'}
        label={'Prev'}
        isHovered={isHovered}
        className={'!w-3 !h-3 bg-blue-500'}
        labelClassName={'absolute bottom-3 right-3 px-4 py-1 bg-blue-100 text-blue-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap'}
      />


      {/*<Handle*/}
      {/*  type="target"*/}
      {/*  position={Position.Top}*/}
      {/*  id="target-top"*/}
      {/*  className={`!w-3 !h-3 !bg-blue-500 ${isHandleVisible('target-top') ? '' : 'opacity-0 pointer-events-none'}`}*/}
      {/*>*/}
      {/*  <div*/}
      {/*    className={`absolute bottom-3 right-3 px-4 py-1 bg-blue-100 text-blue-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap ${*/}
      {/*      isHandleVisible('target-top') ? '' : 'opacity-0'*/}
      {/*    }`}*/}
      {/*  >*/}
      {/*    Prev*/}
      {/*  </div>*/}
      {/*</Handle>*/}

      <Handle
        type="target"
        position={Position.Right}
        id="target-right"
        className={`!w-3 !h-3 bg-blue-500 ${isHandleVisible('target-right') ? '' : 'opacity-0 pointer-events-none'}`}
      >
        <div
          className={`absolute bottom-3 left-3 px-4 py-1 bg-blue-100 text-blue-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap ${
            isHandleVisible('target-right') ? '' : 'opacity-0'
          }`}
        >
          Prev
        </div>
      </Handle>


      {/* Outgoing (Next) */}
      <Handle
        type="source"
        position={Position.Bottom}
        id="source-bottom"
        className={`!w-3 !h-3 bg-green-500 ${isHandleVisible('source-bottom') ? '' : 'opacity-0 pointer-events-none'}`}
        onConnect={() => handleConnect('source-bottom')}
      >
        <div
          className={`absolute top-3 right-3 px-4 py-1 bg-orange-100 text-orange-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap ${
            isHandleVisible('source-bottom') ? '' : 'opacity-0'
          }`}
        >
          Next
        </div>
      </Handle>

      <Handle
        type="source"
        position={Position.Left}
        id="source-left"
        className={`!w-3 !h-3 bg-green-500 ${isHandleVisible('source-left') ? '' : 'opacity-0 pointer-events-none'}`}
        onConnect={() => handleConnect('source-left')}
      >
        <div
          className={`absolute bottom-3 right-3 px-4 py-1 bg-orange-100 text-orange-800 text-xs rounded shadow-sm font-semibold text-center whitespace-nowrap ${
            isHandleVisible('source-left') ? '' : 'opacity-0 pointer-events-none'
          }`}
        >
          Next
        </div>
      </Handle>
    </div>
  );
};

interface IHandleProps {
  type: 'source' | 'target';
  position: Position;
  id: string;
  label: string;
  isHovered: boolean;
  className?: string;
  labelClassName?: string;
}

const HandleWrapper: React.FC<IHandleProps> = ({ type, position, id, label, isHovered, className, labelClassName }) => {
  const [connectedSourceHandles, setConnectedSourceHandles] = useState<string[]>([]);

  const edges = useStore((state) => state.edges);
  const connectedTargetHandles = edges
    .filter(edge => edge.target === id)
    .map(edge => edge.targetHandle || '');

  const handleConnect = (handleId: string) => {
    setConnectedSourceHandles((prev) => [...prev, handleId]);
  };
  const isHandleVisible = (handleId: string): boolean => {
    const isTarget = handleId.startsWith('target');
    const isSource = handleId.startsWith('source');

    const anyTargetConnected = connectedTargetHandles.length > 0;
    const anySourceConnected = connectedSourceHandles.length > 0;

    if (isTarget) {
      if (anyTargetConnected) {
        return connectedTargetHandles.includes(handleId);
      }
      return isHovered;
    }

    if (isSource) {
      if (anySourceConnected) {
        return connectedSourceHandles.includes(handleId);
      }
      return isHovered;
    }

    return false;
  };

  return <Handle
    type={type}
    position={position}
    id={id}
    className={`${className} ${isHandleVisible(id) ? '' : 'opacity-0 pointer-events-none'}`}
    onConnect={() => handleConnect(id)}
  >
    <div
      className={`${labelClassName} ${isHandleVisible(id) ? '' : 'opacity-0 pointer-events-none'}`}
    >
      {label}
    </div>
  </Handle>;
};

export default EndBlock;
