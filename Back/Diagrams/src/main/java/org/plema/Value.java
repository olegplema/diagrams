package org.plema;

public record Value(Object value, String type) {
    public boolean isInt() {
        return "int".equals(type);
    }

    public int asInt() {
        return Integer.parseInt(value.toString());
    }

    public double asDouble() {
        return Double.parseDouble(value.toString());
    }
}
