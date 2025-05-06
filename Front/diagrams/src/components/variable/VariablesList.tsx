import React from 'react';
import { useVariableStore } from '../../store/variableStore';


const VariablesList: React.FC = () => {
  const { variables } = useVariableStore();
  return (
    <>
      {variables.map((v) => (
        <div key={v.name} className="p-2 bg-white rounded mb-2">
          {v.name}: {v.type}
        </div>
      ))}
    </>
  );
};

export default VariablesList;