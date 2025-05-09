import { Edge } from '@xyflow/react';
import { create } from 'zustand';
import { BlockType } from '../types/BlockType';
import { CustomNode, IdMap, IFlowNode, WhileNodesIdMap } from '../types/types';
import { useVariableStore } from './variableStore';
import { findReachableNodes } from '../utils/findReacableNodes';
import { buildIdMap } from '../utils/buildIdMap';
import { processWhileNodes } from '../utils/processWhileBlocks';
import { processConditionNode } from '../utils/processConditionNode';
import { processStandardNode } from '../utils/processCommonNode';
import { processWhileNode } from '../utils/processWhileNode';
import { convertNodesToFlowFormat } from '../utils/convertNodesToFlowFormat';

// Define store state type
interface FlowchartState {
  nodes: CustomNode[];
  edges: Edge[];
  generateJSON: () => {
    variables: ReturnType<typeof useVariableStore.getState>['variables'];
    threads: Array<Array<IFlowNode>>;
  };
}

// Create zustand store
export const useFlowchartStore = create<FlowchartState>((set, get) => ({
  nodes: [],
  edges: [],
  generateJSON: () => {
    const { nodes, edges } = get();
    const { variables } = useVariableStore.getState();

    const startThreadNodes = nodes.filter(n => n.data.type === BlockType.START_THREAD);

    const regularNodes = nodes.filter(n => n.data.type !== BlockType.START_THREAD);

    const threads = startThreadNodes.map(startNode => {
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
