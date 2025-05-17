package org.plema.controllers;

import io.vertx.core.Vertx;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.plema.dtos.CodeResponse;
import org.plema.dtos.MessageResponse;
import org.plema.models.Diagram;
import org.plema.services.GenerateCodeService;
import org.plema.services.RunDiagramService;

public class DiagramController {

    private final GenerateCodeService generateCodeService = new GenerateCodeService();
    private final RunDiagramService runDiagramService;

    public DiagramController(Vertx vertx) {
        this.runDiagramService = new RunDiagramService(vertx);
    }

    public void generateCode(RoutingContext context) {
        try {
            Diagram diagram = context.get("convertedData");
            String code = generateCodeService.generateCode(diagram);
            context.response()
                    .setStatusCode(201)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new CodeResponse("Code generated", code)).encode());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse(e.getMessage())).encode());
        } catch (Exception e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
        }
    }

    public void runDiagram(RoutingContext context) {
        try {
            Diagram diagram = context.get("convertedData");
            String clientSocketId = context.get("clientSocketId");
            runDiagramService.runDiagram(diagram, clientSocketId);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse(e.getMessage())).encode());
        } catch (Exception e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
        }
    }
}
