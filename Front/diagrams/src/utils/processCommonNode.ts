import { CustomNode, IFlowNode, WhileNodesIdMap } from '../types/types';
import { Edge } from '@xyflow/react';
import { BlockType } from '../types/BlockType';
import { getTargetId } from './getTargetId';

export const processStandardNode = (
  node: CustomNode,
  baseNode: IFlowNode,
  idMap: { [key: string]: number },
  edges: Edge[],
  whileNodesInfo: WhileNodesIdMap
): void => {
  // For END nodes, handle their special connection points
  if (node.data.type === BlockType.END) {
    const sourceBottom = getTargetId(node.id, 'source-bottom', idMap, edges);
    const sourceLeft = getTargetId(node.id, 'source-left', idMap, edges);
    baseNode.next = sourceBottom || sourceLeft;
  } else {
    baseNode.next = getTargetId(node.id, 'next', idMap, edges);
  }

  // Handle loop back to WHILE nodes
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
};
