import React from 'react';
import { Node } from '@xyflow/react';
import { NodeData } from '../../types/FlowTypes';

interface NodePropertiesPanelProps {
  selectedNode: Node<NodeData> | null;
  updateNodeData: (id: string, newData: Partial<NodeData>) => void;
  variables: { name: string; type: string }[];
}

export function NodePropertiesPanel({
                                      selectedNode,
                                      updateNodeData,
                                      variables,
                                    }: NodePropertiesPanelProps) {
  if (!selectedNode) return null;

  const data = selectedNode.data;

  return (
    <div className="bg-white p-4 rounded shadow-lg">
    <h3 className="font-bold mb-2">Node Properties</h3>
  {data.nodeType === 'input' && (
    <div>
      <label className="block mb-1">Variable:</label>
  <select
    value={data.variable || ''}
    onChange={(e) => updateNodeData(selectedNode.id, { variable: e.target.value })}
    className="border rounded p-1 w-full"
    >
    <option value="">Select variable</option>
    {variables.map((v, i) => (
      <option key={i} value={v.name}>
      {v.name}
      </option>
    ))}
    </select>
    </div>
  )}
  {data.nodeType === 'output' && (
    <div>
      <label className="block mb-1">Expression:</label>
  <input
    type="text"
    value={data.expression || ''}
    onChange={(e) => updateNodeData(selectedNode.id, { expression: e.target.value })}
    className="border rounded p-1 w-full"
    placeholder="e.g., X or "
      />
      </div>
  )}
  {data.nodeType === 'assign' && (
    <div>
      <label className="block mb-1">Expression:</label>
  <input
    type="text"
    value={data.expression || ''}
    onChange={(e) => updateNodeData(selectedNode.id, { expression: e.target.value })}
    className="border rounded p-1 w-full"
    placeholder="e.g., X = 5 or X = Y + 1"
      />
      </div>
  )}
  {(data.nodeType === 'condition' || data.nodeType === 'while') && (
    <div>
      <label className="block mb-1">Condition:</label>
  <input
    type="text"
    value={data.expression || ''}
    onChange={(e) => updateNodeData(selectedNode.id, { expression: e.target.value })}
    className="border rounded p-1 w-full"
    placeholder="e.g., X < 10 or X == 5"
      />
      </div>
  )}
  </div>
);
}