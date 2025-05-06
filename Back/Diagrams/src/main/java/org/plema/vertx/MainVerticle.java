package org.plema.vertx;

import io.vertx.core.AbstractVerticle;
import io.vertx.core.Promise;
import io.vertx.core.http.HttpMethod;
import io.vertx.ext.web.Router;
import io.vertx.ext.web.handler.CorsHandler;
import org.plema.controllers.DiagramController;
import org.plema.vertx.interfaces.WebSocketLifecycleHandler;

import java.util.HashSet;
import java.util.Set;

public class MainVerticle extends AbstractVerticle {

    @Override
    public void start(Promise<Void> startPromise) {
        Router router = Router.router(vertx);

        Set<HttpMethod> allowedMethods = new HashSet<>();
        allowedMethods.add(HttpMethod.GET);
        allowedMethods.add(HttpMethod.POST);
        allowedMethods.add(HttpMethod.OPTIONS);

        router.route().handler(CorsHandler.create()
                .addRelativeOrigin("http://localhost:3000")
                .allowedMethods(allowedMethods)
                .allowedHeader("Content-Type")
                .allowedHeader("Authorization")
                .allowCredentials(true));

        Router diagramRouter = new DiagramRouter(new DiagramController()).createRouter(vertx);
        router.route("/*").subRouter(diagramRouter);

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