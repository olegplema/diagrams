import { Edge } from '@xyflow/react';
import { create } from 'zustand';
import { BlockType } from '../types/BlockType';
import { CustomNode, IFlowNode } from '../types/types';
import { useVariableStore } from './variableStore';
import { findReachableNodes } from '../utils/findReacableNodes';
import { buildIdMap } from '../utils/buildIdMap';
import { processWhileNodes } from '../utils/processWhileBlocks';
import { convertNodesToFlowFormat } from '../utils/convertNodesToFlowFormat';

interface FlowchartState {
  nodes: CustomNode[];
  edges: Edge[];
  generateJSON: (
    nodes: CustomNode[],
    edges: Edge[]
  ) => {
    variables: ReturnType<typeof useVariableStore.getState>['variables'];
    threads: Array<Array<IFlowNode>>;
  };
}

export const useFlowchartStore = create<FlowchartState>((set, get) => ({
  nodes: [],
  edges: [],
  generateJSON: (nodes: CustomNode[], edges: Edge[]) => {
    //const { nodes, edges } = get();
    const { variables } = useVariableStore.getState();

    const startThreadNodes = nodes.filter(n => n.data.type === BlockType.START_THREAD);

    const regularNodes = nodes.filter(n => n.data.type !== BlockType.START_THREAD);

    const threads = startThreadNodes.map((startNode: { id: string }) => {
      const reachableNodes = findReachableNodes(startNode.id, edges);
      const threadNodes = regularNodes.filter(n => reachableNodes.has(n.id));
      const idMap = buildIdMap(threadNodes);
      const whileNodesInfo = processWhileNodes(threadNodes, edges, reachableNodes);
      return convertNodesToFlowFormat(threadNodes, edges, idMap, whileNodesInfo);
    });

    return {
      variables,
      threads,
    };
  },
}));
