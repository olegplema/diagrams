import React from 'react';
import { Variable } from '../../types/types';
import VariablesList from './VariablesList';
import VariableSelector from './VariableSelector';

const VariableManager: React.FC = () => {
  return (
    <div className="w-64 bg-gray-100 p-4 h-full">
      <h2 className="text-lg font-bold mb-4">Variables</h2>
      <VariableSelector/>
      <VariablesList />
    </div>
  );
};

export default VariableManager;