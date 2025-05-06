import React from 'react';
import { Variable } from '../../types/types';

interface IProps {
  variables: Variable[]
}

const VariablesList: React.FC<IProps> = ({variables}) => {
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