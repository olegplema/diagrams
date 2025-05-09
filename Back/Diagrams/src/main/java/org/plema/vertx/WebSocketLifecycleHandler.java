package org.plema.vertx;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.ServerWebSocketHandshake;

public interface WebSocketLifecycleHandler {
    void handleConnect(ServerWebSocketHandshake handshake);
    void handleMessage(ServerWebSocket webSocket);
}
