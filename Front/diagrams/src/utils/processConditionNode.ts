import { CustomNode, IdMap, IFlowNode } from '../types/types';
import { Edge } from '@xyflow/react';
import { getTargetId } from './getTargetId';

export const processConditionNode = (
  node: CustomNode,
  baseNode: IFlowNode,
  idMap: IdMap,
  edges: Edge[]
): void => {
  baseNode.trueBranch = getTargetId(node.id, 'true', idMap, edges);
  baseNode.falseBranch = getTargetId(node.id, 'false', idMap, edges);
};
