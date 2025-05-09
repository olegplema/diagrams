import { CustomNode, IdMap } from '../types/types';

export const buildIdMap = (nodes: CustomNode[]): IdMap => {
  const idMap: { [key: string]: number } = {};

  nodes.forEach((n, index) => {
    idMap[n.id] = index + 1;
  });

  return idMap;
};
