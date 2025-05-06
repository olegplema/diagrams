import React from 'react';
import { Variable } from '../../types/types';
import VariablesList from './VariablesList';
import VariableSelector from './VariableSelector';


interface IProps {
  variables: Variable[];
  setVariables: (vars: Variable[]) => void;
}


const VariableManager: React.FC<IProps> = ({ variables, setVariables }) => {
  return (
    <div className="w-64 bg-gray-100 p-4 h-full">
      <h2 className="text-lg font-bold mb-4">Variables</h2>
      <VariableSelector variables={variables} setVariables={setVariables} />
      <VariablesList variables={variables} />
    </div>
  );
};

export default VariableManager;