package org.plema.services;

import org.plema.models.AbstractBlock;
import org.plema.visitor.Visitor;

import java.util.List;
import java.util.Map;

public abstract class AbstractDiagramService {

    protected void executeBlocks(AbstractBlock firstBlock, Visitor visitor) {
        AbstractBlock currentBlock = firstBlock;
        while (currentBlock != null) {
            currentBlock = currentBlock.doVisitor(visitor);
        }
    }
}
