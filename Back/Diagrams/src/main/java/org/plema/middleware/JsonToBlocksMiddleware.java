package org.plema.middleware;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.plema.models.AbstractBlock;
import org.plema.models.BlockFactory;
import org.plema.models.Diagram;
import org.plema.models.Variable;

import java.util.ArrayList;
import java.util.List;

public class JsonToBlocksMiddleware implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        List<Variable> variables = new ArrayList<>();
        List<List<AbstractBlock>> threads = new ArrayList<>();

        JsonObject json = routingContext.body().asJsonObject();

        JsonArray variablesJson = json.getJsonArray("variables");
        variablesJson.forEach(variable -> {
            JsonObject variableJson = (JsonObject) variable;
            String type = variableJson.getString("type");
            String name = variableJson.getString("name");

            variables.add(new Variable(type, name));
        });

        BlockFactory blockFactory = new BlockFactory(variables);
        JsonArray threadsJson = json.getJsonArray("threads");
        for (Object t : threadsJson) {
            JsonArray threadJson = (JsonArray) t;
            List<AbstractBlock> thread = new ArrayList<>();

            for (Object b : threadJson) {
                JsonObject blockJson = (JsonObject) b;

                thread.add(blockFactory.createBlock(blockJson));
            }
            threads.add(thread);
        }

        routingContext.put("convertedData", new Diagram(variables, threads));
        routingContext.next();
    }
}
