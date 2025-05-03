package org.plema.vertx;

import io.vertx.core.Vertx;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.BodyHandler;
import org.plema.controllers.DiagramController;
import org.plema.middleware.JsonToBlocksMiddleware;
import org.plema.models.BlockFactory;

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

        router.post("/run");

        return router;
    }
}
