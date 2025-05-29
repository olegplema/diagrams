package org.plema.services.testing;

import java.util.ArrayList;
import java.util.List;

public class MockInput {
    private final List<String> inputs;
    private int currentIndex = 0;

    public MockInput(List<String> inputs) {
        this.inputs = new ArrayList<>(inputs);
    }

    public String readLine() {
        if (currentIndex < inputs.size()) {
            return inputs.get(currentIndex++);
        }
        throw new RuntimeException("No more input available");
    }

    public void reset() {
        currentIndex = 0;
    }
}
