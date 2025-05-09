import { CustomNode, WhileNodeInfo, WhileNodesIdMap } from '../types/types';
import { Edge } from '@xyflow/react';
import { BlockType } from '../types/BlockType';

export const processWhileNodes = (
  nodes: CustomNode[],
  edges: Edge[],
  reachableNodes: Set<string>
): WhileNodesIdMap => {
  const whileNodesInfo: WhileNodesIdMap = {};

  // First pass: identify basic while node connections
  nodes
    .filter(n => n.data.type === BlockType.WHILE)
    .forEach(whileNode => {
      const bodyEdge = edges.find(e => e.source === whileNode.id && e.sourceHandle === 'body');
      const nextEdge = edges.find(e => e.source === whileNode.id && e.sourceHandle === 'next');

      // Initialize the info object for this while node
      whileNodesInfo[whileNode.id] = {
        bodyEntryId: bodyEdge?.target || '', // Empty string if no body
        endNodeId: nextEdge?.target || '', // Empty string if no next
      };

      // For empty while loops, explicitly handle the case
      if (!bodyEdge && nextEdge) {
        // This is an empty while loop - the next edge points directly to the end node
        whileNodesInfo[whileNode.id].endNodeId = nextEdge.target;
      }
    });

  // Second pass: identify nodes that cycle back to while nodes
  Object.keys(whileNodesInfo).forEach(whileId => {
    const cycleBackEdges = edges.filter(e => e.target === whileId && reachableNodes.has(e.source));

    // Remove the sourceHandle check to catch all edges coming back to the while node
    if (cycleBackEdges.length > 0) {
      whileNodesInfo[whileId].lastNodeIds = cycleBackEdges.map(e => e.source);
    }
  });

  return whileNodesInfo;
};
