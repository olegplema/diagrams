package org.plema.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.ext.web.Router;
import org.plema.controllers.DiagramController;
import org.plema.vertx.interfaces.WebSocketLifecycleHandler;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = new DiagramRouter(new DiagramController()).createRouter(vertx);
        WebSocketLifecycleHandler webSocketHandler = WebSocketHandler.getInstance();

        vertx.createHttpServer()
                .webSocketHandshakeHandler(webSocketHandler::handleConnect)
                .webSocketHandler(webSocketHandler::handleMessage)
                .requestHandler(router)
                .listen(8888)
                .onSuccess(server -> {
                    System.out.println("HTTP server started on port " + server.actualPort());
                    startPromise.complete();
                })
                .onFailure(throwable -> {
                    throwable.printStackTrace();
                    startPromise.fail(throwable);
                });
    }
}