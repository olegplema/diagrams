import React, { useState } from 'react';

interface VariablesPanelProps {
  variables: { name: string; type: string }[];
  setVariables: React.Dispatch<React.SetStateAction<{ name: string; type: string }[]>>;
}

export function VariablesPanel({ variables, setVariables }: VariablesPanelProps) {
  const [newVarName, setNewVarName] = useState('');
  const [newVarType, setNewVarType] = useState('int');

  const addVariable = () => {
    if (newVarName && !variables.find((v) => v.name === newVarName)) {
      setVariables([...variables, { name: newVarName, type: newVarType }]);
      setNewVarName('');
    }
  };

  return (
    <div className="bg-white p-4 rounded shadow-lg">
      <h3 className="font-bold mb-2">Variables</h3>
      <div className="mb-2">
        <input
          type="text"
          value={newVarName}
          onChange={(e) => setNewVarName(e.target.value)}
          className="border rounded p-1 mr-2"
          placeholder="Variable name"
        />
        <select
          value={newVarType}
          onChange={(e) => setNewVarType(e.target.value)}
          className="border rounded p-1 mr-2"
        >
          <option value="int">int</option>
        </select>
        <button onClick={addVariable} className="bg-blue-500 text-white px-2 py-1 rounded">
          Add
        </button>
      </div>
      <div className="max-h-32 overflow-y-auto">
        {variables.map((variable, index) => (
          <div key={index} className="flex justify-between items-center mb-1">
            <span>
              {variable.type} {variable.name}
            </span>
            <button
              onClick={() => {
                setVariables(variables.filter((_, i) => i !== index));
              }}
              className="text-red-500 text-sm"
            >
              Remove
            </button>
          </div>
        ))}
      </div>
    </div>
  );
}