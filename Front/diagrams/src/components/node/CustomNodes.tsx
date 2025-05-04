import React from 'react';
import { NodeTypes } from '@xyflow/react';
import { NodeData } from '../../types/FlowTypes';

function InputNode({ data }: { data: NodeData }) {
  return (
    <div className="rounded-lg border-2 border-blue-500 bg-blue-100 p-4 text-center">
    <div className="font-bold">INPUT</div>
      <div>{data.variable}</div>
      </div>
  );
}

function OutputNode({ data }: { data: NodeData }) {
  return (
    <div className="rounded-lg border-2 border-green-500 bg-green-100 p-4 text-center">
    <div className="font-bold">PRINT</div>
      <div>{data.expression}</div>
      </div>
  );
}

function AssignNode({ data }: { data: NodeData }) {
  return (
    <div className="rounded-lg border-2 border-purple-500 bg-purple-100 p-4 text-center">
    <div className="font-bold">ASSIGN</div>
      <div>{data.expression}</div>
      </div>
  );
}

function ConditionNode({ data }: { data: NodeData }) {
  return (
    <div className="rounded-lg border-2 border-orange-500 bg-orange-100 p-4 text-center diamond-shape">
    <div className="font-bold">CONDITION</div>
      <div>{data.expression}</div>
      </div>
  );
}

function WhileNode({ data }: { data: NodeData }) {
  return (
    <div className="rounded-lg border-2 border-yellow-500 bg-yellow-100 p-4 text-center diamond-shape">
    <div className="font-bold">WHILE</div>
      <div>{data.expression}</div>
      </div>
  );
}

function EndConditionNode({ data }: { data: NodeData }) {
  return (
    <div className="rounded-lg border-2 border-gray-500 bg-gray-100 p-4 text-center">
    <div className="font-bold">END CONDITION</div>
  </div>
);
}

function EndNode({ data }: { data: NodeData }) {
  return (
    <div className="rounded-lg border-2 border-red-500 bg-red-100 p-4 text-center">
    <div className="font-bold">END</div>
      </div>
  );
}

export const nodeTypes: NodeTypes = {
  input: InputNode,
  output: OutputNode,
  assign: AssignNode,
  condition: ConditionNode,
  while: WhileNode,
  end_condition: EndConditionNode,
  end: EndNode,
};