package org.plema.controllers;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.plema.CodeRunner;
import org.plema.dtos.MessageResponse;
import org.plema.models.Diagram;
import org.plema.services.GenerateCodeService;
import org.plema.services.RunDiagramService;

public class DiagramController {

    private final GenerateCodeService generateCodeService = new GenerateCodeService();
    private final RunDiagramService runDiagramService = new RunDiagramService();
    private final CodeRunner codeRunner = new CodeRunner();

    public void generateCode(RoutingContext context) {
        try {
            Diagram diagram = context.get("convertedData");
            System.out.println(generateCodeService.generateCode(diagram));
        } catch (Exception e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
        }

        context.response()
                .setStatusCode(201)
                .putHeader("Content-Type", "application/json")
                .end(JsonObject.mapFrom(new MessageResponse("Code generated")).encode());
    }

    public void runDiagram(RoutingContext context) {
        try {
//            JsonNode blocks = new ObjectMapper().readTree(context.body().asString());
//            JsonNode threads = blocks.get("threads");
//            JsonNode firstThread = threads.get(0);
//            codeRunner.runBlocks(firstThread);
            Diagram diagram = context.get("convertedData");
            runDiagramService.runDiagram(diagram);
        } catch (Exception e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
        }

        context.response()
                .setStatusCode(201)
                .putHeader("Content-Type", "application/json")
                .end(JsonObject.mapFrom(new MessageResponse("Code generated")).encode());

    }
}
