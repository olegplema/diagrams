package org.plema.vertx;

import java.util.concurrent.ExecutionException;

public interface WebSocketMessageSender {
    void sendMessage(String sessionId, String message);
    String sendAndWaitResult(String sessionId, String type, String message) throws ExecutionException, InterruptedException;
}