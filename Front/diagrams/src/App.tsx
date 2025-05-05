import React, { useCallback, useState } from 'react';
import {
  ReactFlow,
  Background,
  Controls,
  MiniMap,
  useNodesState,
  useEdgesState,
  addEdge,
  Handle,
  Position,
  Node,
  Edge,
  Connection,
} from '@xyflow/react';
import '@xyflow/react/dist/style.css';

// Types
type NodeType = 'start_thread' | 'input' | 'assign' | 'while' | 'condition' | 'print' | 'end';
type VariableType = 'int' | string;
type Variable = { name: string; type: VariableType };

interface FlowNode {
  id: number;
  type: Exclude<NodeType, 'start_thread'>;
  variable?: string;
  expression?: string;
  next?: number | null;
  trueBranch?: number;
  falseBranch?: number;
  body?: number;
}

interface FlowThread {
  id: number;
  nodes: FlowNode[];
  edges: Edge[];
}

interface NodeData {
  type: NodeType;
  variable?: string;
  expression?: string;
  setVariable?: (variable: string) => void;
  setExpression?: (expression: string) => void;
  variables: Variable[];
  deleteNode?: () => void;
  [key: string]: unknown;
}

interface CustomNode extends Node<NodeData, 'custom'> {
  data: NodeData;
}

// Node Component
const FlowNode = ({ data, id }: { data: NodeData; id: string }) => {
  const { type, variable, expression, setVariable, setExpression, variables, deleteNode } = data;
  const isCondition = type === 'condition';
  const isStartThread = type === 'start_thread';
  const isConfigurable = type === 'input' || type === 'assign' || type === 'while' || type === 'condition' || type === 'print';

  const validateExpression = (value: string): boolean => {
    if (!value) return false;
    if (type === 'input') return true;
    if (type === 'assign') return /^\w+\s*=\s*(\w+|\d+|"[^"]*")$/.test(value);
    if (type === 'while' || type === 'condition') return /^\w+\s*(==|<|>)\s*\d+$/.test(value);
    if (type === 'print') return /^\w+$|^"[^"]*"$/.test(value);
    return true;
  };

  return (
    <div className={`bg-white border-2 ${isStartThread ? 'border-blue-500' : 'border-gray-300'} rounded-lg p-4 shadow-md min-w-[200px] relative`}>
      <button
        onClick={deleteNode}
        className="absolute top-1 right-1 text-red-500 hover:text-red-700 text-lg font-bold"
        title="Delete node"
      >
        ×
      </button>
      {!isStartThread && (
        <Handle type="target" position={Position.Top} className="w-[18px] h-[18px] bg-blue-500" />
      )}
      <div className="font-bold text-center">{type.toUpperCase()}</div>
      {isConfigurable && (
        <div className="mt-2">
          {type !== 'while' && type !== 'condition' && (
            <div className="mb-2">
              <label className="block text-sm">Variable:</label>
              <select
                value={variable || ''}
                onChange={(e) => setVariable?.(e.target.value)}
                className="w-full p-1 border rounded"
              >
                <option value="">Select variable</option>
                {variables.map((v) => (
                  <option key={v.name} value={v.name}>{v.name}</option>
                ))}
              </select>
            </div>
          )}
          {type !== 'input' && (
            <div>
              <label className="block text-sm">Expression:</label>
              <input
                type="text"
                value={expression || ''}
                onChange={(e) => setExpression?.(e.target.value)}
                className={`w-full p-1 border rounded ${validateExpression(expression || '') ? '' : 'border-red-500'}`}
                placeholder={
                  type === 'assign' ? 'e.g., X = 5 or X = Y' :
                    type === 'while' || type === 'condition' ? 'e.g., X < 5 or X == 5 or X > 5' :
                      type === 'print' ? 'e.g., X or "text"' : ''
                }
              />
            </div>
          )}
        </div>
      )}
      {!isCondition && (
        <Handle type="source" position={Position.Bottom} id="next" className="w-[18px] h-[18px] bg-blue-500" />
      )}
      {isCondition && (
        <>
          <div className="relative">
            <Handle type="source" position={Position.Left} id="true" className="w-[18px] h-[18px] bg-green-500" />
            <span className="absolute left-[-40px] top-[-6px] text-xs font-bold text-green-700">true</span>
          </div>
          <div className="relative">
            <Handle type="source" position={Position.Right} id="false" className="w-[18px] h-[18px] bg-red-500" />
            <span className="absolute right-[-40px] top-[-6px] text-xs font-bold text-red-700">false</span>
          </div>
        </>
      )}
    </div>
  );
};

// Sidebar Component
const Sidebar = ({ onAddNode }: { onAddNode: (type: NodeType) => void }) => {
  const nodeTypes: NodeType[] = ['start_thread', 'input', 'assign', 'while', 'condition', 'print', 'end'];
  return (
    <div className="w-64 bg-gray-100 p-4 h-full">
      <h2 className="text-lg font-bold mb-4">Add Nodes</h2>
      {nodeTypes.map((type) => (
        <button
          key={type}
          onClick={() => onAddNode(type)}
          className="w-full bg-blue-500 text-white p-2 rounded mb-2 hover:bg-blue-600"
        >
          {type.toUpperCase()}
        </button>
      ))}
    </div>
  );
};

// Variable Manager Component
const VariableManager = ({
                           variables,
                           setVariables,
                         }: {
  variables: Variable[];
  setVariables: (vars: Variable[]) => void;
}) => {
  const [newVar, setNewVar] = useState('');
  const [varType, setVarType] = useState<VariableType>('int');

  const addVariable = () => {
    if (newVar && !variables.find((v) => v.name === newVar)) {
      setVariables([...variables, { name: newVar, type: varType }]);
      setNewVar('');
      setVarType('int');
    }
  };

  return (
    <div className="w-64 bg-gray-100 p-4 h-full">
      <h2 className="text-lg font-bold mb-4">Variables</h2>
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
          <option value="int">int</option>
        </select>
        <button onClick={addVariable} className="bg-green-500 text-white p-2 rounded-b">
          Add
        </button>
      </div>
      {variables.map((v) => (
        <div key={v.name} className="p-2 bg-white rounded mb-2">
          {v.name}: {v.type}
        </div>
      ))}
    </div>
  );
};

// Flowchart Editor Component
const FlowchartEditor = () => {
  const [threads, setThreads] = useState<FlowThread[]>([{ id: 1, nodes: [], edges: [] }]);
  const [variables, setVariables] = useState<Variable[]>([]);
  const [nodes, setNodes, onNodesChange] = useNodesState<CustomNode>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);
  const [nodeIdCounter, setNodeIdCounter] = useState(0);

  const onConnect = useCallback(
    (params: Connection) => {
      const sourceNode = nodes.find((n) => n.id === params.source);
      if (!sourceNode) return;

      if (sourceNode.data.type !== 'condition') {
        if (edges.some((e) => e.source === params.source && e.sourceHandle === 'next')) return;
        setEdges((eds) => addEdge({ ...params, id: `${params.source}-${params.target}` }, eds));
      } else {
        if (edges.some((e) => e.source === params.source && e.sourceHandle === params.sourceHandle)) return;
        setEdges((eds) =>
          addEdge(
            {
              ...params,
              id: `${params.source}-${params.target}-${params.sourceHandle}`,
              type: params.sourceHandle === 'true' ? 'smoothstep' : 'step',
            },
            eds
          )
        );
      }
    },
    [nodes, edges, setEdges]
  );

  const deleteNode = useCallback(
    (nodeId: string) => {
      setNodes((nds) => nds.filter((n) => n.id !== nodeId));
      setEdges((eds) => eds.filter((e) => e.source !== nodeId && e.target !== nodeId));
    },
    [setNodes, setEdges]
  );

  const onAddNode = useCallback(
    (type: NodeType) => {
      const id = `${nodeIdCounter}`;
      const newNode: CustomNode = {
        id,
        type: 'custom',
        position: { x: Math.random() * 300, y: Math.random() * 300 },
        data: {
          type,
          variable: type !== 'end' ? variables[0]?.name || '' : undefined,
          expression: type !== 'end' ? '' : undefined,
          setVariable: type !== 'end' ? (variable: string) => {
            setNodes((nds) =>
              nds.map((n) => (n.id === id ? { ...n, data: { ...n.data, variable } } : n))
            );
          } : undefined,
          setExpression: type !== 'end' ? (expression: string) => {
            setNodes((nds) =>
              nds.map((n) => (n.id === id ? { ...n, data: { ...n.data, expression } } : n))
            );
          } : undefined,
          variables,
          deleteNode: () => deleteNode(id),
        },
      };
      setNodes((nds) => [...nds, newNode]);
      setNodeIdCounter((prev) => prev + 1);

      if (type === 'start_thread') {
        setThreads((prev) => [...prev, { id: prev.length + 1, nodes: [], edges: [] }]);
      }
    },
    [variables, setNodes, nodeIdCounter, deleteNode]
  );

  const exportToJson = () => {
    const startThreadNodes = nodes.filter((n) => n.data.type === 'start_thread');
    const regularNodes = nodes.filter((n) => n.data.type !== 'start_thread');

    // Визначаємо які вузли належать кожному потоку
    const getThreadNodeIds = (startId: string): Set<string> => {
      const visited = new Set<string>();
      const stack = [startId];
      while (stack.length > 0) {
        const current = stack.pop()!;
        const children = edges
          .filter(e => e.source === current)
          .map(e => e.target)
          .filter(t => !visited.has(t));
        children.forEach(child => {
          visited.add(child);
          stack.push(child);
        });
      }
      return visited;
    };

    const threads: FlowNode[][] = startThreadNodes.map((startNode) => {
      const threadNodeIds = getThreadNodeIds(startNode.id);
      const threadNodes = regularNodes.filter(n => threadNodeIds.has(n.id));

      // Створити локальні id для цього потоку
      const idMap: Record<string, number> = {};
      threadNodes.forEach((n, index) => {
        idMap[n.id] = index + 1;
      });

      // Знайти всі while блоки в потоці
      const whileNodes = threadNodes.filter(n => n.data.type === 'while');

      // Знайти всі end блоки
      const endNodes = threadNodes.filter(n => n.data.type === 'end');

      // Зберігає інформацію про те, який блок перед end і який while з ним пов'язаний
      const whileBlocksInfo: Record<string, {
        lastNodeBeforeEnd: string | null,
        endNodeId: string | null
      }> = {};

      // Для кожного while блоку шукаємо останній блок перед end блоком
      whileNodes.forEach(whileNode => {
        // Починаємо з блоку, на який вказує body вказівник while
        const bodyEdge = edges.find(e => e.source === whileNode.id && e.sourceHandle === 'next');
        if (!bodyEdge) return;

        let currentNodeId = bodyEdge.target;
        let foundEndNode = false;
        let lastNodeBeforeEnd: string | null = null;
        let endNodeId: string | null = null;

        // Проходимо по шляху від першого блоку тіла циклу
        while (!foundEndNode) {
          const currentNode = threadNodes.find(n => n.id === currentNodeId);
          if (!currentNode) break;

          // Знаходимо наступний блок за поточним
          const nextEdge = edges.find(e => e.source === currentNodeId && e.sourceHandle === 'next');
          if (!nextEdge) break;

          const nextNodeId = nextEdge.target;
          const nextNode = threadNodes.find(n => n.id === nextNodeId);

          // Якщо наступний блок - end, запам'ятовуємо поточний як останній перед end
          if (nextNode && nextNode.data.type === 'end') {
            lastNodeBeforeEnd = currentNodeId;
            endNodeId = nextNodeId;
            foundEndNode = true;
            break;
          }

          // Переходимо до наступного блоку
          currentNodeId = nextNodeId;
        }

        whileBlocksInfo[whileNode.id] = { lastNodeBeforeEnd, endNodeId };
      });

      const getTargetId = (sourceId: string, handle: string): number | undefined => {
        const edge = edges.find(e => e.source === sourceId && e.sourceHandle === handle);
        return edge?.target && idMap[edge.target] ? idMap[edge.target] : undefined;
      };

      const flowNodes: FlowNode[] = threadNodes.map((node) => {
        // Базовий об'єкт для вузла
        const baseNode: FlowNode = {
          id: idMap[node.id],
          type: node.data.type as Exclude<NodeType, 'start_thread'>,
          variable: node.data.variable,
          expression: node.data.expression,
        };

        // Додаємо специфічні поля в залежності від типу вузла
        if (node.data.type === 'condition') {
          baseNode.trueBranch = getTargetId(node.id, 'true');
          baseNode.falseBranch = getTargetId(node.id, 'false');
        } else if (node.data.type === 'while') {
          // Додаємо тіло циклу
          const bodyEdge = edges.find(e => e.source === node.id && e.sourceHandle === 'next');
          if (bodyEdge) {
            baseNode.body = idMap[bodyEdge.target];
          }

          // Знаходимо end блок, до якого веде шлях від цього while
          const whileInfo = whileBlocksInfo[node.id];
          if (whileInfo && whileInfo.endNodeId) {
            baseNode.next = idMap[whileInfo.endNodeId];
          }
        } else {
          // Перевіряємо, чи це останній блок перед end в якомусь циклі
          let isLastBlockBeforeEnd = false;
          let whileNodeId: string | null = null;

          for (const [wNodeId, info] of Object.entries(whileBlocksInfo)) {
            if (info.lastNodeBeforeEnd === node.id) {
              isLastBlockBeforeEnd = true;
              whileNodeId = wNodeId;
              break;
            }
          }

          if (isLastBlockBeforeEnd && whileNodeId) {
            // Якщо це останній блок перед end в циклі while, вказуємо на while
            baseNode.next = idMap[whileNodeId];
          } else {
            // Інакше використовуємо звичайний next
            baseNode.next = getTargetId(node.id, 'next');
          }
        }

        return baseNode;
      });

      return flowNodes;
    });

    const json = {
      variables,
      threads
    };

    const blob = new Blob([JSON.stringify(json, null, 2)], { type: 'application/json' });
    const url = URL.createObjectURL(blob);
    const a = document.createElement('a');
    a.href = url;
    a.download = 'flowchart.json';
    a.click();
    URL.revokeObjectURL(url);
  };
  const isDescendant = (startId: string, targetId: string, edges: Edge[]): boolean => {
    const visited = new Set<string>();
    const stack = [startId];

    while (stack.length > 0) {
      const currentId = stack.pop()!;
      if (currentId === targetId) return true;
      if (visited.has(currentId)) continue;
      visited.add(currentId);

      const outgoingEdges = edges.filter((e) => e.source === currentId);
      for (const edge of outgoingEdges) {
        stack.push(edge.target);
      }
    }
    return false;
  };

  return (
    <div className="flex h-screen">
      <Sidebar onAddNode={onAddNode} />
      <div className="flex-1">
        <div className="p-4 bg-gray-200">
          <button
            onClick={exportToJson}
            className="bg-blue-500 text-white px-4 py-2 rounded hover:bg-blue-600"
          >
            Export to JSON
          </button>
        </div>
        <ReactFlow
          nodes={nodes}
          edges={edges}
          onNodesChange={onNodesChange}
          onEdgesChange={onEdgesChange}
          onConnect={onConnect}
          nodeTypes={{ custom: FlowNode }}
          fitView
        >
          <Background />
          <Controls />
          <MiniMap />
        </ReactFlow>
      </div>
      <VariableManager variables={variables} setVariables={setVariables} />
    </div>
  );
};

// App Component
const App: React.FC = () => {
  return (
    <div className="min-h-screen bg-gray-50">
      <header className="bg-blue-600 text-white p-4 shadow-md">
        <h1 className="text-2xl font-bold">Flowchart Editor</h1>
      </header>
      <main className="p-4">
        <FlowchartEditor />
      </main>
    </div>
  );
};

export default App;