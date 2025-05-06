import React, { useCallback, useState } from 'react';
import { CustomNode, IFlowNode, Variable } from '../../types/types';
import {
  addEdge,
  Background,
  Connection,
  Controls,
  Edge,
  MiniMap,
  ReactFlow,
  useEdgesState,
  useNodesState,
} from '@xyflow/react';
import { BlockType } from '../../types/BlockType';
import { useCodeGeneration } from '../../hooks/useCodeGeneration';
import CodeModal from '../CodeModal';
import FlowNode from '../blocks/FlowBlock';
import Sidebar from '../sidebar/Sidebar';
import VariableManager from '../variable/VariableManager';
import { useVariableStore } from '../../store/variableStore';

const FlowchartEditor = () => {
  const { variables } = useVariableStore();
  const [nodes, setNodes, onNodesChange] = useNodesState<CustomNode>([]);
  const [edges, setEdges, onEdgesChange] = useEdgesState<Edge>([]);
  const [nodeIdCounter, setNodeIdCounter] = useState(0);

  const onConnect = useCallback(
    (params: Connection) => {
      const sourceNode = nodes.find((n) => n.id === params.source);
      if (!sourceNode) return;

      if (sourceNode.data.type !== BlockType.CONDITION) {
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
            eds,
          ),
        );
      }
    },
    [nodes, edges, setEdges],
  );

  const deleteNode = useCallback(
    (nodeId: string) => {
      setNodes((nds) => nds.filter((n) => n.id !== nodeId));
      setEdges((eds) => eds.filter((e) => e.source !== nodeId && e.target !== nodeId));
    },
    [setNodes, setEdges],
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
                setNodes((nds) =>
                  nds.map((n) => (n.id === id ? { ...n, data: { ...n.data, variable } } : n)),
                );
              }
              : undefined,
          setExpression:
            type !== BlockType.END
              ? (expression: string) => {
                setNodes((nds) =>
                  nds.map((n) => (n.id === id ? { ...n, data: { ...n.data, expression } } : n)),
                );
              }
              : undefined,
          deleteNode: () => deleteNode(id),
        },
      };
      setNodes((nds) => [...nds, newNode]);
      setNodeIdCounter((prev) => prev + 1);
    },
    [variables, setNodes, nodeIdCounter, deleteNode],
  );

  const handleGenerateCode = () => {
    const startThreadNodes = nodes.filter((n) => n.data.type === BlockType.START_THREAD);
    const regularNodes = nodes.filter((n) => n.data.type !== BlockType.START_THREAD);

    const getThreadNodeIds = (startId: string): Set<string> => {
      const visited = new Set<string>();
      const stack = [startId];
      while (stack.length > 0) {
        const current = stack.pop()!;
        const children = edges
          .filter((e) => e.source === current)
          .map((e) => e.target)
          .filter((t) => !visited.has(t));
        children.forEach((child) => {
          visited.add(child);
          stack.push(child);
        });
      }
      return visited;
    };

    const threads: IFlowNode[][] = startThreadNodes.map((startNode) => {
      const threadNodeIds = getThreadNodeIds(startNode.id);
      const threadNodes = regularNodes.filter((n) => threadNodeIds.has(n.id));

      const idMap: Record<string, number> = {};
      threadNodes.forEach((n, index) => {
        idMap[n.id] = index + 1;
      });

      const whileNodes = threadNodes.filter((n) => n.data.type === BlockType.WHILE);

      const whileBlocksInfo: Record<
        string,
        {
          lastNodeBeforeEnd: string | null;
          endNodeId: string | null;
        }
      > = {};

      whileNodes.forEach((whileNode) => {
        const bodyEdge = edges.find((e) => e.source === whileNode.id && e.sourceHandle === 'next');
        if (!bodyEdge) return;

        let currentNodeId = bodyEdge.target;
        let foundEndNode = false;
        let lastNodeBeforeEnd: string | null = null;
        let endNodeId: string | null = null;

        while (!foundEndNode) {
          const currentNode = threadNodes.find((n) => n.id === currentNodeId);
          if (!currentNode) break;

          const nextEdge = edges.find((e) => e.source === currentNodeId && e.sourceHandle === 'next');
          if (!nextEdge) break;

          const nextNodeId = nextEdge.target;
          const nextNode = threadNodes.find((n) => n.id === nextNodeId);

          if (nextNode && nextNode.data.type === BlockType.END) {
            lastNodeBeforeEnd = currentNodeId;
            endNodeId = nextNodeId;
            foundEndNode = true;
            break;
          }

          currentNodeId = nextNodeId;
        }

        whileBlocksInfo[whileNode.id] = { lastNodeBeforeEnd, endNodeId };
      });

      const getTargetId = (sourceId: string, handle: string): number | undefined => {
        const edge = edges.find((e) => e.source === sourceId && e.sourceHandle === handle);
        return edge?.target && idMap[edge.target] ? idMap[edge.target] : undefined;
      };

      return threadNodes.map((node) => {
        const baseNode: IFlowNode = {
          id: idMap[node.id],
          type: node.data.type as Exclude<BlockType, BlockType.START_THREAD>,
          variable: node.data.variable,
          expression: node.data.expression,
        };

        if (node.data.type === BlockType.CONDITION) {
          baseNode.trueBranch = getTargetId(node.id, 'true');
          baseNode.falseBranch = getTargetId(node.id, 'false');
        } else if (node.data.type === BlockType.WHILE) {
          const bodyEdge = edges.find((e) => e.source === node.id && e.sourceHandle === 'next');
          if (bodyEdge) {
            baseNode.body = idMap[bodyEdge.target];
          }

          const whileInfo = whileBlocksInfo[node.id];
          if (whileInfo && whileInfo.endNodeId) {
            baseNode.next = idMap[whileInfo.endNodeId];
          }
        } else {
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
            baseNode.next = idMap[whileNodeId];
          } else {
            baseNode.next = getTargetId(node.id, 'next');
          }
        }

        return baseNode;
      });
    });

    const json = {
      variables,
      threads,
    };

    return generate(json);
  };

  const { generatedCodeData, generate } = useCodeGeneration();

  return (
    <div className="flex h-screen">
      <Sidebar onAddNode={onAddNode} />
      <div className="flex-1">
        <div className="p-4 bg-gray-200">
          <CodeModal onClick={handleGenerateCode} generatedCodeData={generatedCodeData} />
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
      <VariableManager />
    </div>
  );
};

export default FlowchartEditor;