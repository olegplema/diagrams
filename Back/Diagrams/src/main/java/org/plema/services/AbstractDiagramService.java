package org.plema.services;

import org.plema.models.AbstractBlock;
import org.plema.visitor.Visitor;

import java.util.List;
import java.util.Map;

abstract class AbstractDiagramService {

    protected void executeBlocks(List<AbstractBlock> blocks, Map<Integer, AbstractBlock> blockMap, Visitor visitor) {
        Integer currentBlockId = 1;
        for (AbstractBlock block : blocks) {
            blockMap.put(block.getId(), block);
        }

        while (blockMap.containsKey(currentBlockId)) {
            AbstractBlock block = blockMap.get(currentBlockId);
            currentBlockId = block.doVisitor(visitor);
        }
    }
}
