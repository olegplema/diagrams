package org.plema.vertx.interfaces;

public interface WebSocketMessageSender {
    void sendMessage(String sessionId, String message);
}