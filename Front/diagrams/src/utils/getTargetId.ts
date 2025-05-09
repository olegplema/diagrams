import { Edge } from '@xyflow/react';
import { IdMap } from '../types/types';

export const getTargetId = (
  sourceId: string,
  handle: string | null,
  idMap: IdMap,
  edges: Edge[]
): number | undefined => {
  const edge = edges.find(
    e => e.source === sourceId && (handle ? e.sourceHandle === handle : true)
  );

  return edge?.target && idMap[edge.target] ? idMap[edge.target] : undefined;
};
