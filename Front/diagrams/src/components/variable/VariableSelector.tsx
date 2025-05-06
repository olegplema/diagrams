import React, { useState } from 'react';
import { VariableType } from '../../types/VariableType';
import { JAVA_KEYWORDS } from '../../constants/javaKeywords';
import { Variable } from '../../types/types';

interface IProps {
  variables: Variable[];
  setVariables: (vars: Variable[]) => void;
}

const regexValidator = /^[a-zA-Z_$][a-zA-Z_$0-9]*$/;

const VariableSelector: React.FC<IProps> = ({ variables, setVariables }) => {
  const [newVar, setNewVar] = useState('');
  const [varType, setVarType] = useState<VariableType>(VariableType.INT);

  const validateVariable = (name: string) => {
    return !(!regexValidator.test(name) || JAVA_KEYWORDS.includes(name.toLowerCase()) || variables.find((v) => v.name === name));

  };
  const addVariable = () => {
    if (newVar && validateVariable(newVar)) {
      setVariables([...variables, { name: newVar, type: varType }]);
      setNewVar('');
      setVarType(varType);
    } else {
      setNewVar('');
      alert('Variable name error');
    }
  };

  return (
    <div className="flex flex-col mb-4">
      <input
        type="text"
        value={newVar}
        onChange={(e) => setNewVar(e.target.value)}
        className="p-2 border rounded-t mb-2"
        placeholder="Variable name"
      />
      <select
        value={varType}
        onChange={(e) => setVarType(e.target.value as VariableType)}
        className="p-2 border rounded mb-2"
      >
        {Object.values(VariableType).map((v) => (<option key={v} value={v}>{v}</option>))}
      </select>
      <button onClick={addVariable} className="bg-green-500 text-white p-2 rounded-b">
        Add
      </button>
    </div>
  );
};

export default VariableSelector;