import { CustomNode, IdMap, IFlowNode, WhileNodesIdMap } from '../types/types';

export const processWhileNode = (
  node: CustomNode,
  baseNode: IFlowNode,
  idMap: IdMap,
  whileNodesInfo: WhileNodesIdMap
): void => {
  const whileInfo = whileNodesInfo[node.id];
  if (whileInfo) {
    // Only set body if we have a valid bodyEntryId that's different from next
    if (whileInfo.bodyEntryId && whileInfo.bodyEntryId !== whileInfo.endNodeId) {
      baseNode.body = idMap[whileInfo.bodyEntryId];
    }

    // Set next if we have a valid endNodeId
    if (whileInfo.endNodeId) {
      baseNode.next = idMap[whileInfo.endNodeId];
    }
  }
};
