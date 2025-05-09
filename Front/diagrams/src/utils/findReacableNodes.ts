import { Edge } from '@xyflow/react';

export const findReachableNodes = (startNodeId: string, edges: Edge[]): Set<string> => {
  const reachableNodes = new Set<string>();
  const stack = [startNodeId];

  while (stack.length > 0) {
    const current = stack.pop()!;

    edges
      .filter(e => e.source === current)
      .forEach(edge => {
        const targetId = edge.target;
        if (!reachableNodes.has(targetId)) {
          reachableNodes.add(targetId);
          stack.push(targetId);
        }
      });
  }

  return reachableNodes;
};
