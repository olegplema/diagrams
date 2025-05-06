import { NodeData } from '../../types/types';
import React from 'react';
import { Handle, Position } from '@xyflow/react';
import CloseButton from '../buttons/CloseButton';

interface IProps {
  data: NodeData;
  id: string;
}

const EndBlock: React.FC<IProps> = ({ data }) => {
  return (
    <div className="bg-white border-2 border-gray-300 rounded-lg p-4 shadow-md min-w-[200px] relative">
      <CloseButton onClick={data.deleteNode} />

      <Handle type="target" position={Position.Top} className="w-[18px] h-[18px] bg-blue-500" />
      <div className="font-bold text-center">END</div>
    </div>
  );
};

export default EndBlock;