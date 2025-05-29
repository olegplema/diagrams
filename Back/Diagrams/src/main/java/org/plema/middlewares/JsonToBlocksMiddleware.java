package org.plema.middlewares;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.plema.DataType;
import org.plema.dtos.Diagram;
import org.plema.models.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class JsonToBlocksMiddleware implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        List<Variable> variables = new ArrayList<>();
        List<AbstractBlock> threads = new ArrayList<>();
        Map<Integer, AbstractBlock> blockMap = new HashMap<>();

        JsonObject json = routingContext.body().asJsonObject();

        JsonArray variablesJson = json.getJsonArray("variables");
        variablesJson.forEach(variable -> {
            JsonObject variableJson = (JsonObject) variable;
            String type = variableJson.getString("type");
            String name = variableJson.getString("name");

            variables.add(new Variable(name, DataType.valueOf(type.toUpperCase())));
        });

        BlockFactory blockFactory = new BlockFactory(variables);
        JsonArray threadsJson = json.getJsonArray("threads");

        for (Object t : threadsJson) {
            JsonArray threadJson = (JsonArray) t;

            for (Object b : threadJson) {
                JsonObject blockJson = (JsonObject) b;
                AbstractBlock currentBlock = blockFactory.createBlock(blockJson);
                blockMap.put(currentBlock.getId(), currentBlock);
            }
        }

        for (AbstractBlock block : blockMap.values()) {
            Integer nextId = block.getNextId();
            if (nextId != null) {
                block.setNext(blockMap.get(nextId));
            }

            if (block instanceof ConditionBlock conditionBlock) {
                Integer trueBranchId = conditionBlock.getTrueBranchId();
                Integer falseBranchId = conditionBlock.getFalseBranchId();

                if (trueBranchId != null) {
                    conditionBlock.setTrueBranch(blockMap.get(trueBranchId));
                }

                if (falseBranchId != null) {
                    conditionBlock.setFalseBranch(blockMap.get(falseBranchId));
                }
            } else if (block instanceof WhileBlock whileBlock) {
                Integer bodyId = whileBlock.getBodyId();
                if (bodyId != null) {
                    whileBlock.setBody(blockMap.get(bodyId));
                }
            }
        }

        for (Object t : threadsJson) {
            JsonArray threadJson = (JsonArray) t;
            if (!threadJson.isEmpty()) {
                JsonObject firstBlockJson = threadJson.getJsonObject(0);
                Integer firstBlockId = firstBlockJson.getInteger("id");
                AbstractBlock firstBlock = blockMap.get(firstBlockId);
                if (firstBlock != null) {
                    threads.add(firstBlock);
                }
            }
        }

        String clientSocketId = json.getString("clientSocketId");

        if (clientSocketId != null) {
            routingContext.put("clientSocketId", clientSocketId);
        }

        routingContext.put("convertedData", new Diagram(variables, threads, blockMap));
        routingContext.next();
    }
}
