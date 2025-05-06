import React from 'react';
import { MouseEventHandler } from 'react';

interface IProps {
  onClick?: MouseEventHandler<HTMLButtonElement>;
}

const CloseButton: React.FC<IProps> = ({ onClick }) => {
  return (
    <div>
      <button
        onClick={onClick}
        className="absolute top-1 right-1 text-red-500 hover:text-red-700 text-lg font-bold"
        title="Delete node"
      >
        ×
      </button>
    </div>
  );
};

export default CloseButton;