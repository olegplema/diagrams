package org.plema;

public record Value(Object value, String type) {
    public boolean isInt() {
        return "int".equals(type);
    }

    public boolean isDouble() {
        return "double".equals(type);
    }

    public int asInt() {
        if (value instanceof Integer) {
            return (Integer) value;
        } else if (value instanceof Double) {
            return ((Double) value).intValue();
        } else if (value instanceof String && isInteger((String) value)) {
            return Integer.parseInt((String) value);
        }
        throw new ClassCastException("Cannot cast " + value + " to int");
    }

    public double asDouble() {
        if (value instanceof Double) {
            return (Double) value;
        } else if (value instanceof Integer) {
            return ((Integer) value).doubleValue();
        } else if (value instanceof String && isNumber((String) value)) {
            return Double.parseDouble((String) value);
        }
        throw new ClassCastException("Cannot cast " + value + " to double");
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
