package org.plema.vertx;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.ServerWebSocketHandshake;
import org.plema.vertx.interfaces.WebSocketLifecycleHandler;
import org.plema.vertx.interfaces.WebSocketMessageSender;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

public class WebSocketHandler implements WebSocketLifecycleHandler, WebSocketMessageSender {
    private final Map<String, ServerWebSocket> sessions = new ConcurrentHashMap<>();

    private WebSocketHandler() {}

    private static class Holder {
        private static final WebSocketHandler INSTANCE = new WebSocketHandler();
    }

    public static WebSocketHandler getInstance() {
        return Holder.INSTANCE;
    }

    @Override
    public void sendMessage(String sessionId, String message) {
        ServerWebSocket webSocket = sessions.get(sessionId);
        if (webSocket != null) {
            webSocket.writeTextMessage(message);
        }
    }

    @Override
    public void handleConnect(ServerWebSocketHandshake handshake) {
        if (handshake.path().equals("/connect-ws")) {
            handshake.reject();
        } else {
            System.out.println("Connecting to " + handshake.path());
            handshake.accept();
        }
    }

    @Override
    public void handleMessage(ServerWebSocket webSocket) {
        if (!"/connect-ws".equals(webSocket.path())) {
            webSocket.close((short) 1003, "Invalid path");
            return;
        }

        String sessionId = webSocket.remoteAddress().toString();
        sessions.put(sessionId, webSocket);

        webSocket.closeHandler(v -> {
            System.out.println("WebSocket closed: " + sessionId);
            sessions.remove(sessionId);
        });

        webSocket.writeTextMessage(sessionId);
    }
}
