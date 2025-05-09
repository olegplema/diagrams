package org.plema.vertx;

import io.vertx.core.http.ServerWebSocket;
import io.vertx.core.http.ServerWebSocketHandshake;
import io.vertx.core.json.JsonObject;
import org.plema.dtos.WebSocketReceive;

import java.util.Map;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public class WebSocketHandler implements WebSocketLifecycleHandler, WebSocketMessageSender {
    private final Map<String, ServerWebSocket> sessions = new ConcurrentHashMap<>();
    private final ConcurrentHashMap<String, CompletableFuture<String>> pendingResponses = new ConcurrentHashMap<>();

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
    public String sendAndWaitResult(String sessionId, String type, String message)
            throws ExecutionException, InterruptedException {
        ServerWebSocket webSocket = sessions.get(sessionId);

        if (webSocket != null) {
            webSocket.writeTextMessage(JsonObject.mapFrom(new WebSocketHandler()).encode());
            CompletableFuture<String> future = new CompletableFuture<>();
            pendingResponses.put(sessionId, future);

            webSocket.textMessageHandler(future::complete);

            return future.get();
        }

        throw new IllegalStateException("WebSocket session not connected for sessionId: " + sessionId);
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

            CompletableFuture<String> future = pendingResponses.remove(sessionId);
            if (future != null && !future.isDone()) {
                future.completeExceptionally(new IllegalStateException("WebSocket closed before response"));
            }
        });

        webSocket.writeTextMessage(sessionId);
    }
}
