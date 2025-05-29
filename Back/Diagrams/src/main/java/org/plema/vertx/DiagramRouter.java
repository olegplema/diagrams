package org.plema.vertx;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.RoutingContext;
import io.vertx.ext.web.handler.BodyHandler;
import org.plema.controllers.DiagramController;
import org.plema.dtos.Diagram;
import org.plema.dtos.NamedTestCase;
import org.plema.dtos.TestCaseData;
import org.plema.dtos.TestingResult;
import org.plema.middlewares.JsonToBlocksMiddleware;
import org.plema.middlewares.TestCaseParsingMiddleware;
import org.plema.services.testing.TestDiagramService;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

public class DiagramRouter {
    private final DiagramController diagramController;

    public DiagramRouter(DiagramController diagramController) {
        this.diagramController = diagramController;
    }

    public Router createRouter(Vertx vertx) {
        Router router = Router.router(vertx);

        router.post("/generate-code")
                .handler(BodyHandler.create())
                .handler(new JsonToBlocksMiddleware())
                .handler(diagramController::generateCode);

        router.post("/run")
                .handler(BodyHandler.create())
                .handler(new JsonToBlocksMiddleware())
                .handler(diagramController::runDiagram);

        router.post("/test/interactive")
                .handler(BodyHandler.create())
                .handler(new JsonToBlocksMiddleware())
                .handler(new TestCaseParsingMiddleware())
                .handler(diagramController::handleInteractiveTest);

        router.post("/test/suite")
                .handler(BodyHandler.create())
                .handler(new JsonToBlocksMiddleware())
                .handler(new TestCaseParsingMiddleware())
                .handler(diagramController::handleTestSuite);

        return router;
    }
}
