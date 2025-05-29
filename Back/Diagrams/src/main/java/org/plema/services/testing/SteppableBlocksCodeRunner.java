package org.plema.services.testing;

import org.plema.Value;
import org.plema.models.AbstractBlock;
import org.plema.visitor.runner.BlocksCodeRunner;

import java.util.Map;


public class SteppableBlocksCodeRunner {
    private final BlocksCodeRunner runner;
    private AbstractBlock currentBlock;
    private boolean isDone;
    private int executedSteps;

    public SteppableBlocksCodeRunner(Map<String, Value> variables, MockWebSocketHandler ioHandler, String clientId) {
        this.runner = new BlocksCodeRunner(variables, ioHandler, clientId);
        this.executedSteps = 0;
        this.isDone = false;
    }

    public void setCurrentBlock(AbstractBlock block) {
        this.currentBlock = block;
    }

    public boolean isDone() {
        return isDone || currentBlock == null;
    }

    public void step() {
        if (isDone() || currentBlock == null) {
            return;
        }

        try {
            currentBlock = currentBlock.doVisitor(runner);
            executedSteps++;

            if (currentBlock == null) {
                isDone = true;
            }
        } catch (Exception e) {
            isDone = true;
        }
    }

    public int getExecutedSteps() {
        return executedSteps;
    }
}