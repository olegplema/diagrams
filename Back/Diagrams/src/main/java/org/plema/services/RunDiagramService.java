package org.plema.services;

import org.plema.Value;
import org.plema.models.AbstractBlock;
import org.plema.models.Diagram;
import org.plema.visitor.runner.BlocksCodeRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RunDiagramService extends AbstractDiagramService{

    public void runDiagram(Diagram diagram) {
        Map<String, Value> variableMap = new HashMap<>();

        for (var variable : diagram.variables()) {
            variableMap.put(variable.getName(), new Value(variable.getType().getDefaultValue(), variable.getType()));
        }

        try (ExecutorService executor = Executors.newVirtualThreadPerTaskExecutor()) {
            int numberOfThreads = diagram.threads().size();
            CountDownLatch latch = new CountDownLatch(numberOfThreads);

            for (var thread : diagram.threads()) {
                executor.execute(() -> {
                    try {
                        Map<Integer, AbstractBlock> blockMap = new HashMap<>();
                        BlocksCodeRunner blocksCodeRunner = new BlocksCodeRunner(variableMap);

                        executeBlocks(thread, blockMap, blocksCodeRunner);
                    } finally {
                        latch.countDown();
                    }
                });
            }

            latch.await();

            System.out.println("All threads have completed execution.");
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
}
