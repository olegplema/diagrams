import React from 'react';
import { useVariableStore } from '../../store/variableStore';

const VariablesList: React.FC = () => {
  const { variables } = useVariableStore();
  return (
    <div className={'overflow-y-scroll max-h-[64vh]'}>
      {variables.map(v => (
        <div key={v.name} className="p-2 bg-white rounded mb-2">
          {v.name}: {v.type}
        </div>
      ))}
    </div>
  );
};

export default VariablesList;
