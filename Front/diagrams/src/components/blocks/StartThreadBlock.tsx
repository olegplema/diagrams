import { NodeData } from '../../types/types';
import { Handle, Position } from '@xyflow/react';
import React from 'react';
import CloseButton from '../buttons/CloseButton';

interface IProps {
  data: NodeData;
  id: string;
}

const StartThreadBlock: React.FC<IProps> = ({ data }) => {
  return (
    <div className="bg-white border-2 border-blue-500 rounded-lg p-4 shadow-md min-w-[200px] relative">
      <CloseButton onClick={data.deleteNode} />

      <div className="font-bold text-center">START_THREAD</div>
      <Handle type="source" position={Position.Bottom} id="next" className="w-[18px] h-[18px] bg-blue-500" />
    </div>
  );
};

export default StartThreadBlock;