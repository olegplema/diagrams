import React from 'react';
import { LuTriangle } from 'react-icons/lu';

interface IProps {
  handleClick: () => void;
}

const RunCodeButton: React.FC<IProps> = ({ handleClick }) => {
  return (
    <button
      onClick={handleClick}
      className="bg-green-400 text-white px-2 py-2 rounded hover:bg-green-600 transition-colors flex items-center justify-center"
    >
      {LuTriangle({ className: 'rotate-90', size: 24 }) as React.ReactElement}
    </button>
  );
};

export default RunCodeButton;
