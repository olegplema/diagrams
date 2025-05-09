import { CustomNode, IFlowNode, IdMap, WhileNodesIdMap } from '../types/types';
import { Edge } from '@xyflow/react';
import { BlockType } from '../types/BlockType';

export const convertNodesToFlowFormat = (
  nodes: CustomNode[],
  edges: Edge[],
  idMap: IdMap,
  whileNodesInfo: WhileNodesIdMap
): IFlowNode[] => {
  return nodes.map(node => {
    const flowNode: IFlowNode = {
      id: idMap[node.id],
      type: node.data.type as Exclude<BlockType, BlockType.START_THREAD>,
    };

    // Add variable and expression if they exist
    if (node.data.variable) {
      flowNode.variable = node.data.variable;
    }
    if (node.data.expression) {
      flowNode.expression = node.data.expression;
    }

    // Handle different node types
    if (node.data.type === BlockType.CONDITION) {
      // Find true and false branch targets
      const trueEdge = edges.find(e => e.source === node.id && e.sourceHandle === 'true');
      const falseEdge = edges.find(e => e.source === node.id && e.sourceHandle === 'false');

      if (trueEdge && idMap[trueEdge.target]) {
        flowNode.trueBranch = idMap[trueEdge.target];
      }
      if (falseEdge && idMap[falseEdge.target]) {
        flowNode.falseBranch = idMap[falseEdge.target];
      }
    } else if (node.data.type === BlockType.WHILE) {
      // Handle while nodes
      const whileInfo = whileNodesInfo[node.id];

      // Set body pointer if there's a body entry
      if (whileInfo?.bodyEntryId && idMap[whileInfo.bodyEntryId]) {
        flowNode.body = idMap[whileInfo.bodyEntryId];
      }

      // Set next pointer if there's an end node
      if (whileInfo?.endNodeId && idMap[whileInfo.endNodeId]) {
        flowNode.next = idMap[whileInfo.endNodeId];
      } else {
        // For empty while loops or loops without explicit end nodes
        flowNode.next = null;
      }
    } else {
      // For regular nodes, find next node
      const nextEdge = edges.find(
        e => e.source === node.id && (!e.sourceHandle || e.sourceHandle === 'next')
      );

      // Check if this is a node that cycles back to a while node
      let isLastNodeInWhileLoop = false;

      for (const whileId in whileNodesInfo) {
        if (whileNodesInfo[whileId].lastNodeIds?.includes(node.id)) {
          // This node is the last in a while loop, so its next should point back to the while node
          flowNode.next = idMap[whileId];
          isLastNodeInWhileLoop = true;
          break;
        }
      }

      // If not a loop end node, set normal next pointer
      if (!isLastNodeInWhileLoop && nextEdge && idMap[nextEdge.target]) {
        flowNode.next = idMap[nextEdge.target];
      } else if (!isLastNodeInWhileLoop) {
        // End of a thread or branch
        flowNode.next = null;
      }
    }

    return flowNode;
  });
};
