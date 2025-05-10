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

      // For empty while loops, we might only have a "next" edge
      if (nextEdge) {
        whileNodesInfo[whileNode.id] = {
          bodyEntryId: bodyEdge?.target || nextEdge.target, // If no body, use next as placeholder
          endNodeId: nextEdge.target,
        };
      } else if (bodyEdge) {
        // If somehow we have a body but no next, still capture the body
        whileNodesInfo[whileNode.id] = {
          bodyEntryId: bodyEdge.target,
          endNodeId: '', // Empty string to indicate no end pointer
        };
      }
    });

  // Second pass: identify nodes that cycle back to while nodes
  Object.keys(whileNodesInfo).forEach(whileId => {
    const cycleBackEdges = edges.filter(
      e => e.target === whileId && e.sourceHandle === 'next' && reachableNodes.has(e.source)
    );

    if (cycleBackEdges.length > 0) {
      whileNodesInfo[whileId].lastNodeIds = cycleBackEdges.map(e => e.source);
    }
  });

  return whileNodesInfo;
};
