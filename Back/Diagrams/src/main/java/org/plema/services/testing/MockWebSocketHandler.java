package org.plema.services.testing;

import org.plema.vertx.WebSocketMessageSender;

import java.util.concurrent.ExecutionException;

public class MockWebSocketHandler implements WebSocketMessageSender {
    private final MockOutput stdout;
    private final MockInput stdin;

    public MockWebSocketHandler(MockOutput stdout, MockInput stdin) {
        this.stdout = stdout;
        this.stdin = stdin;
    }

    @Override
    public void sendMessage(String clientId, String message) {
        stdout.println(message);
    }

    @Override
    public String sendAndWaitResult(String clientId, String type, String message)
            throws ExecutionException, InterruptedException {
        if ("input".equals(type)) {
            return stdin.readLine();
        }
        return "";
    }
}
