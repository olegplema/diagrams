import { Handle, Position } from '@xyflow/react';
import { NodeData } from '../../types/types';
import React, { useEffect } from 'react';
import CloseButton from '../buttons/CloseButton';
import { useVariableStore } from '../../store/variableStore';
import HandleWrapper from '../wrapper/HandleWrapper';

interface IProps {
  data: NodeData;
  id: string;
}

const InputNode: React.FC<IProps> = ({ data }) => {
  const { variables } = useVariableStore();

  useEffect(() => {
    if (!data.variable && variables.length > 0 && data.setVariable) {
      data.setVariable(variables[0].name);
    }
  }, [variables]);

  return (
    <div className="bg-white border-2 border-gray-300 rounded-lg p-4 shadow-md min-w-[200px] relative">
      <CloseButton onClick={data.deleteNode} />
      <div className="font-bold text-center">INPUT</div>
      <div className="mt-2">
        <div className="mb-2">
          <label className="block text-sm">Variable:</label>
          <select
            value={data.variable || ''}
            onChange={e => data.setVariable?.(e.target.value)}
            className="w-full p-1 border rounded"
          >
            {variables.length <= 0 ? (
              <option value="">Select variable</option>
            ) : (
              variables.map(v => (
                <option key={v.name} value={v.name}>
                  {v.name}
                </option>
              ))
            )}
          </select>
        </div>
      </div>

      <HandleWrapper type={'target'} position={Position.Top} className={'!w-3 !h-3 bg-blue-500'} />

      <HandleWrapper
        type={'source'}
        position={Position.Bottom}
        id={'next'}
        className={'!w-3 !h-3 bg-blue-500'}
      />
    </div>
  );
};

export default InputNode;
