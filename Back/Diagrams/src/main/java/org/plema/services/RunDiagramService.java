package org.plema.services;

import io.vertx.core.Future;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.plema.Value;
import org.plema.models.AbstractBlock;
import org.plema.models.Diagram;
import org.plema.vertx.WebSocketHandler;
import org.plema.visitor.runner.BlocksCodeRunner;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;

public class RunDiagramService extends AbstractDiagramService {

    private final Vertx vertx;

    public RunDiagramService(Vertx vertx) {
        this.vertx = vertx;
    }

    public Future<Void> runDiagram(Diagram diagram, String clientSocketId) {
        Promise<Void> promise = Promise.promise();

        vertx.executeBlocking(blockingPromise -> {
            try {
                executeBlocks(diagram, clientSocketId);
                blockingPromise.complete();
            } catch (Exception e) {
                blockingPromise.fail(e);
            }
        }, asyncResult -> {
            if (asyncResult.succeeded()) {
                promise.complete();
            } else {
                promise.fail(asyncResult.cause());
            }
        });

        return promise.future();
    }

    private void executeBlocks(Diagram diagram, String clientSocketId) {
        Map<String, Value> variableMap = new HashMap<>();
        for (var variable : diagram.variables()) {
            variableMap.put(variable.getName(), new Value(variable.getType().getDefaultValue(), variable.getType()));
        }

        CompletableFuture<?>[] futures = new CompletableFuture[diagram.threads().size()];

        int i = 0;
        for (var thread : diagram.threads()) {
            CompletableFuture<Void> future = CompletableFuture.runAsync(() -> {
                try {
                    Map<Integer, AbstractBlock> blockMap = new HashMap<>();
                    BlocksCodeRunner blocksCodeRunner = new BlocksCodeRunner(
                            variableMap,
                            WebSocketHandler.getInstance(),
                            clientSocketId
                    );
                    executeBlocks(thread, blockMap, blocksCodeRunner);
                } catch (Exception e) {
                    System.err.println("Error executing thread: " + e.getMessage());
                    e.printStackTrace();
                }
            });
            futures[i++] = future;
        }

        try {
            CompletableFuture.allOf(futures).get();
            System.out.println("All threads have completed execution.");
        } catch (Exception e) {
            System.err.println("Error waiting for threads to complete: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
