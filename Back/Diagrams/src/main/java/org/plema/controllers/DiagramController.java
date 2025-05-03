package org.plema.controllers;

import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.plema.CodeRunner;
import org.plema.dtos.MessageResponse;
import org.plema.models.Diagram;
import org.plema.services.DiagramService;

public class DiagramController {

    private final DiagramService diagramService = new DiagramService();
    private final CodeRunner codeRunner = new CodeRunner();

    public void generateCode(RoutingContext context) {
        try {
            Diagram diagram = context.get("convertedData");
            System.out.println(diagramService.generateCode(diagram));
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

//    public void runDiagram(RoutingContext context) {
//        try {
//            codeRunner.runBlocks();
//            System.out.println(diagramService.generateCode(diagram));
//        } catch (Exception e) {
//            e.printStackTrace();
//            context.response()
//                    .setStatusCode(500)
//                    .putHeader("Content-Type", "application/json")
//                    .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
//        }
//
//        context.response()
//                .setStatusCode(201)
//                .putHeader("Content-Type", "application/json")
//                .end(JsonObject.mapFrom(new MessageResponse("Code generated")).encode());
//
//    }
}
