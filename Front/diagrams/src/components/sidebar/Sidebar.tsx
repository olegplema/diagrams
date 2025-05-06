import React from 'react';
import { BlockType } from '../../types/BlockType';

interface IProps {
  onAddNode: (type: BlockType) => void;
}

const Sidebar: React.FC<IProps> = ({ onAddNode }) => {
  const nodeTypes: BlockType[] = Object.values(BlockType);
  return (
    <div className="w-64 bg-gray-100 p-4 h-full">
      <h2 className="text-lg font-bold mb-4">Add Nodes</h2>
      {nodeTypes.map((type) => (
        <button
          key={type}
          onClick={() => onAddNode(type)}
          className="w-full bg-blue-500 text-white p-2 rounded mb-2 hover:bg-blue-600"
        >
          {type.toUpperCase()}
        </button>
      ))}
    </div>
  );
};

export default Sidebar;