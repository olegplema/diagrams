import React from 'react';

export function Sidebar() {
  const onDragStart = (event: React.DragEvent, nodeType: string, nodeSubType: string) => {
    event.dataTransfer.setData('application/reactflow', nodeType);
    event.dataTransfer.setData('nodeSubType', nodeSubType);
    event.dataTransfer.effectAllowed = 'move';
  };

  return (
    <div className="w-48 bg-gray-100 p-4 border-r border-gray-300 h-full">
    <div className="font-bold mb-4">Node Types</div>
  <div
  className="border border-blue-500 bg-blue-100 p-2 mb-2 rounded cursor-grab"
  onDragStart={(event) => onDragStart(event, 'default', 'input')}
  draggable
  >
  Input
  </div>
  <div
  className="border border-green-500 bg-green-100 p-2 mb-2 rounded cursor-grab"
  onDragStart={(event) => onDragStart(event, 'default', 'output')}
  draggable
  >
  Print
  </div>
  <div
  className="border border-purple-500 bg-purple-100 p-2 mb-2 rounded cursor-grab"
  onDragStart={(event) => onDragStart(event, 'default', 'assign')}
  draggable
  >
  Assign
  </div>
  <div
  className="border border-orange-500 bg-orange-100 p-2 mb-2 rounded cursor-grab"
  onDragStart={(event) => onDragStart(event, 'default', 'condition')}
  draggable
  >
  Condition
  </div>
  <div
  className="border border-yellow-500 bg-yellow-100 p-2 mb-2 rounded cursor-grab"
  onDragStart={(event) => onDragStart(event, 'default', 'while')}
  draggable
  >
  While
  </div>
  <div
  className="border border-gray-500 bg-gray-100 p-2 mb-2 rounded cursor-grab"
  onDragStart={(event) => onDragStart(event, 'default', 'end_condition')}
  draggable
  >
  End Condition
  </div>
  <div
  className="border border-red-500 bg-red-100 p-2 mb-2 rounded cursor-grab"
  onDragStart={(event) => onDragStart(event, 'default', 'end')}
  draggable
  >
  End
  </div>
  </div>
);
}