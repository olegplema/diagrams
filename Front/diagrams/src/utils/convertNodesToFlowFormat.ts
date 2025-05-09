import { CustomNode, IFlowNode, WhileNodesIdMap } from '../types/types';
import { Edge } from '@xyflow/react';
import { BlockType } from '../types/BlockType';
import { processConditionNode } from './processConditionNode';
import { processWhileNode } from './processWhileNode';
import { processStandardNode } from './processCommonNode';

export const convertNodesToFlowFormat = (
  nodes: CustomNode[],
  edges: Edge[],
  idMap: { [key: string]: number },
  whileNodesInfo: WhileNodesIdMap
): IFlowNode[] =>
  nodes.map(node => {
    const baseNode: IFlowNode = {
      id: idMap[node.id],
      type: node.data.type as Exclude<BlockType, BlockType.START_THREAD>,
      variable: node.data.variable,
      expression: node.data.expression,
    };

    switch (node.data.type) {
      case BlockType.CONDITION:
        processConditionNode(node, baseNode, idMap, edges);
        break;
      case BlockType.WHILE:
        processWhileNode(node, baseNode, idMap, whileNodesInfo);
        break;
      default:
        processStandardNode(node, baseNode, idMap, edges, whileNodesInfo);
        break;
    }

    return baseNode;
  });