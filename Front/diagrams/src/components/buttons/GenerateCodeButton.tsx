import React from 'react';
import { IoCodeSlash } from 'react-icons/io5';

interface IProps {
  handleClick: () => void;
}

const GenerateCodeButton: React.FC<IProps> = ({ handleClick }) => {
  return (
    <button
      onClick={handleClick}
      className="bg-blue-500 text-white px-2 py-2 rounded hover:bg-blue-600 transition-colors"
    >
      {IoCodeSlash({ size: 24 }) as React.ReactElement}
    </button>
  );
};

export default GenerateCodeButton;
