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

      const { type } = sourceNode.data;

      const isDuplicate = edges.some(
        (e) => e.source === params.source && e.sourceHandle === params.sourceHandle
      );
      if (isDuplicate) return;

      const edgeType =
        type === BlockType.CONDITION || type === BlockType.WHILE
          ? 'step'
          : 'smoothstep';

      const edgeId = params.sourceHandle
        ? `${params.source}-${params.target}-${params.sourceHandle}`
        : `${params.source}-${params.target}`;

      setEdges((eds) =>
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
      setNodes((nds) => nds.filter((n) => n.id !== nodeId));
      setEdges((eds) => eds.filter((e) => e.source !== nodeId && e.target !== nodeId));
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
                setNodes((nds) =>
                  nds.map((n) => (n.id === id ? { ...n, data: { ...n.data, variable } } : n))
                );
              }
              : undefined,
          setExpression:
            type !== BlockType.END
              ? (expression: string) => {
                setNodes((nds) =>
                  nds.map((n) => (n.id === id ? { ...n, data: { ...n.data, expression } } : n))
                );
              }
              : undefined,
          deleteNode: () => deleteNode(id),
        },
      };
      setNodes((nds) => [...nds, newNode]);
      setNodeIdCounter((prev) => prev + 1);
    },
    [variables, setNodes, nodeIdCounter, deleteNode]
  );

  const handleGenerateCode = () => {
    // Find all START_THREAD nodes to identify thread starting points
    const startThreadNodes = nodes.filter((n) => n.data.type === BlockType.START_THREAD);
    // Get all nodes that aren't START_THREAD nodes
    const regularNodes = nodes.filter((n) => n.data.type !== BlockType.START_THREAD);

    // Build a map of string IDs to numeric IDs for each thread
    const buildIdMap = (threadNodes: CustomNode[]) => {
      const idMap: { [key: string]: number } = {};
      threadNodes.forEach((n, index) => {
        idMap[n.id] = index + 1;
      });
      return idMap;
    };

    // Find node by ID in a thread
    const findNodeById = (threadNodes: CustomNode[], id: string) =>
      threadNodes.find((n) => n.id === id);

    // Get target node ID from source node and handle
    const getTargetId = (sourceId: string, handle: string | null, idMap: { [key: string]: number }) => {
      const edge = edges.find(
        (e) => e.source === sourceId && (handle ? e.sourceHandle === handle : true)
      );
      return edge?.target && idMap[edge.target] ? idMap[edge.target] : undefined;
    };

    // Process each thread starting from a START_THREAD node
    const threads = startThreadNodes.map((startNode) => {
      // Get all nodes reachable from this START_THREAD node
      const reachableNodes = new Set<string>();
      const stack = [startNode.id];

      while (stack.length > 0) {
        const current = stack.pop()!;

        // Find all outgoing edges from current node
        edges
          .filter((e) => e.source === current)
          .forEach((edge) => {
            const targetId = edge.target;
            if (!reachableNodes.has(targetId)) {
              reachableNodes.add(targetId);
              stack.push(targetId);
            }
          });
      }

      // Get all regular nodes that are part of this thread
      const threadNodes = regularNodes.filter((n) => reachableNodes.has(n.id));
      const idMap = buildIdMap(threadNodes);

      // Map of while nodes to their info (body entry point, end node)
      const whileNodesInfo: { [key: string]: { bodyEntryId: string; endNodeId: string; lastNodeIds?: string[] } } = {};

      // Find all while nodes in this thread
      threadNodes
        .filter((n) => n.data.type === BlockType.WHILE)
        .forEach((whileNode) => {
          // Find the body entry edge
          const bodyEdge = edges.find((e) => e.source === whileNode.id && e.sourceHandle === 'body');
          const nextEdge = edges.find((e) => e.source === whileNode.id && e.sourceHandle === 'next');

          if (bodyEdge && nextEdge) {
            whileNodesInfo[whileNode.id] = {
              bodyEntryId: bodyEdge.target,
              endNodeId: nextEdge.target,
            };
          }
        });

      // For each while body, find the last node that connects back to the while node
      Object.keys(whileNodesInfo).forEach((whileId) => {
        const cycleBackEdges = edges.filter(
          (e) => e.target === whileId && e.sourceHandle === 'next' && reachableNodes.has(e.source)
        );

        if (cycleBackEdges.length > 0) {
          whileNodesInfo[whileId].lastNodeIds = cycleBackEdges.map((e) => e.source);
        }
      });

      // Create flow nodes with proper numbering and connections
      return threadNodes.map((node) => {
        const baseNode: IFlowNode = {
          id: idMap[node.id],
          type: node.data.type as Exclude<BlockType, BlockType.START_THREAD>,
          variable: node.data.variable,
          expression: node.data.expression,
        };

        // Handle different node types
        if (node.data.type === BlockType.CONDITION) {
          baseNode.trueBranch = getTargetId(node.id, 'true', idMap);
          baseNode.falseBranch = getTargetId(node.id, 'false', idMap);
        } else if (node.data.type === BlockType.WHILE) {
          const whileInfo = whileNodesInfo[node.id];
          if (whileInfo) {
            baseNode.body = idMap[whileInfo.bodyEntryId];
            baseNode.next = idMap[whileInfo.endNodeId];
          }
        } else {
          // For regular nodes (including END nodes), check for next connections
          // For END nodes, check both source handles (left and bottom)
          if (node.data.type === BlockType.END) {
            // Check for connections from source handles
            const sourceBottom = getTargetId(node.id, 'source-bottom', idMap);
            const sourceLeft = getTargetId(node.id, 'source-left', idMap);
            baseNode.next = sourceBottom || sourceLeft; // Prioritize bottom, fallback to left
          } else {
            baseNode.next = getTargetId(node.id, 'next', idMap);
          }

          // Special case: If this is the last node in a while body
          let isLastNodeInWhileBody = false;
          let whileNodeId = null;

          for (const [wId, info] of Object.entries(whileNodesInfo)) {
            if (info.lastNodeIds && info.lastNodeIds.includes(node.id)) {
              isLastNodeInWhileBody = true;
              whileNodeId = wId;
              break;
            }
          }

          if (isLastNodeInWhileBody && whileNodeId) {
            baseNode.next = idMap[whileNodeId];
          }
        }

        return baseNode;
      });
    });

    // Generate the final JSON structure
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
        </ReactFlow>
      </div>
      <VariableManager />
    </div>
  );
};

export default FlowchartEditor;