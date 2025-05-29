package org.plema.services.testing;

import java.util.ArrayList;
import java.util.List;

public class MockOutput {
    private final List<String> lines = new ArrayList<>();

    public void println(String line) {
        lines.add(line);
    }

    public List<String> getLines() {
        return new ArrayList<>(lines);
    }

    public void clear() {
        lines.clear();
    }
}
