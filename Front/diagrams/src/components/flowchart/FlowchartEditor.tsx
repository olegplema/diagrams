import React, { useCallback } from 'react';
import {
  Background,
  Connection,
  Controls,
  Edge,
  MiniMap,
  ReactFlow,
  addEdge,
  useEdgesState,
  useNodesState,
} from '@xyflow/react';
import { BlockType } from '../../types/BlockType';
import { CustomNode } from '../../types/types';
import { useCodeGeneration } from '../../hooks/useCodeGeneration';
import { useStartRunCode } from '../../hooks/useStartRunCode';
import { useVariableStore } from '../../store/variableStore';
import GenerateCodeModal from '../modal/CodeModal';
import FlowNode from '../blocks/FlowBlock';
import Sidebar from '../sidebar/Sidebar';
import VariableManager from '../variable/VariableManager';
import StartRunningCodeModal from '../modal/CodeRunnerModal';
import { useFlowchartStore } from '../../store/useFlowchartStore';

const FlowchartEditor = () => {
  // Hooks
  const { generatedCodeData, generate } = useCodeGeneration();
  const { startRunningCode } = useStartRunCode();
  const { variables } = useVariableStore();

  // Local node state
  const [nodes, setNodes, onNodesChange] = useNodesState<CustomNode>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);
  const [nodeIdCounter, setNodeIdCounter] = React.useState(0);

  // Update global state whenever local state changes
  React.useEffect(() => {
    useFlowchartStore.setState({ nodes, edges });
  }, [nodes, edges]);

  const onConnect = useCallback(
    (params: Connection) => {
      const sourceNode = nodes.find(n => n.id === params.source);
      if (!sourceNode) return;

      const { type } = sourceNode.data;
      const isDuplicate = edges.some(
        e => e.source === params.source && e.sourceHandle === params.sourceHandle
      );

      if (isDuplicate) return;

      const edgeType =
        type === BlockType.CONDITION || type === BlockType.WHILE ? 'step' : 'smoothstep';
      const edgeId = params.sourceHandle
        ? `${params.source}-${params.target}-${params.sourceHandle}`
        : `${params.source}-${params.target}`;

      setEdges(eds =>
        addEdge(
          {
            ...params,
            id: edgeId,
            type: edgeType,
          },
          eds
        )
      );
    },
    [nodes, edges, setEdges]
  );

  const deleteNode = useCallback(
    (nodeId: string) => {
      setNodes(nds => nds.filter(n => n.id !== nodeId));
      setEdges(eds => eds.filter(e => e.source !== nodeId && e.target !== nodeId));
    },
    [setNodes, setEdges]
  );

  const onAddNode = useCallback(
    (type: BlockType) => {
      const id = `${nodeIdCounter}`;
      const newNode: CustomNode = {
        id,
        type: 'custom',
        position: { x: Math.random() * 300, y: Math.random() * 300 },
        data: {
          type,
          variable: type !== BlockType.END ? variables[0]?.name || '' : undefined,
          expression: '',
          setVariable:
            type !== BlockType.END
              ? (variable: string) => {
                  setNodes(nds =>
                    nds.map(n => (n.id === id ? { ...n, data: { ...n.data, variable } } : n))
                  );
                }
              : undefined,
          setExpression:
            type !== BlockType.END
              ? (expression: string) => {
                  setNodes(nds =>
                    nds.map(n => (n.id === id ? { ...n, data: { ...n.data, expression } } : n))
                  );
                }
              : undefined,
          deleteNode: () => deleteNode(id),
        },
      };

      setNodes(nds => [...nds, newNode]);
      setNodeIdCounter(prev => prev + 1);
    },
    [variables, setNodes, nodeIdCounter, deleteNode]
  );

  // Connect to JSON generator and code generators
  const { generateJSON } = useFlowchartStore();

  const handleGenerateCode = () => {
    const jsonData = generateJSON();
    console.log(JSON.stringify(jsonData, null, 2));
    return generate(jsonData);
  };

  const handleStartRunningCode = () => {
    const jsonData = generateJSON();
    return startRunningCode(jsonData);
  };

  return (
    <div className="flex h-screen">
      <Sidebar onAddNode={onAddNode} />
      <div className="flex-1">
        <div className="p-4 bg-gray-200 flex gap-3">
          <GenerateCodeModal onClick={handleGenerateCode} generatedCodeData={generatedCodeData} />
          <StartRunningCodeModal onClick={handleStartRunningCode} />
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
        </ReactFlow>
      </div>
      <VariableManager />
    </div>
  );
};

export default FlowchartEditor;
