package org.plema;

public record Value(Object value, DataType type) {

    public boolean isInt() {
        return type == DataType.INT;
    }

    public boolean isDouble() {
        return type == DataType.DOUBLE;
    }

    public boolean isString() {
        return type == DataType.STRING;
    }

    public boolean isBoolean() {
        return type == DataType.BOOLEAN;
    }

    public int asInt() {
        if (isInt()) {
            return (Integer) value;
        } else if (isDouble()) {
            return ((Double) value).intValue();
        } else if (isBoolean()) {
            // Конвертація з boolean в int (true = 1, false = 0)
            return ((Boolean) value) ? 1 : 0;
        } else if (isString()) {
            try {
                return Integer.parseInt((String) value);
            } catch (NumberFormatException e) {
                throw new ClassCastException("Cannot cast string '" + value + "' to int");
            }
        }
        throw new ClassCastException("Cannot cast " + type + " to int");
    }

    public double asDouble() {
        if (isDouble()) {
            return (Double) value;
        } else if (isInt()) {
            return ((Integer) value).doubleValue();
        } else if (isBoolean()) {
            // Конвертація з boolean в double (true = 1.0, false = 0.0)
            return ((Boolean) value) ? 1.0 : 0.0;
        } else if (isString()) {
            try {
                return Double.parseDouble((String) value);
            } catch (NumberFormatException e) {
                throw new ClassCastException("Cannot cast string '" + value + "' to double");
            }
        }
        throw new ClassCastException("Cannot cast " + type + " to double");
    }

    public String asString() {
        return String.valueOf(value);
    }

    public boolean asBoolean() {
        if (isBoolean()) {
            return (Boolean) value;
        } else if (isInt()) {
            // Числа, відмінні від нуля, вважаються true
            return (Integer) value != 0;
        } else if (isDouble()) {
            // Числа, відмінні від нуля, вважаються true
            return (Double) value != 0.0;
        } else if (isString()) {
            // Рядок "true" (без урахування регістру) вважається true
            return Boolean.parseBoolean((String) value);
        }
        throw new ClassCastException("Cannot cast " + type + " to boolean");
    }

    @Override
    public String toString() {
        if (isBoolean()) {
            return Boolean.toString((Boolean) value);
        }
        return String.valueOf(value);
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) return true;
        if (obj == null || getClass() != obj.getClass()) return false;

        Value other = (Value) obj;

        if (type == other.type) {
            return value.equals(other.value);
        }

        // Handle numeric type comparison
        if ((isInt() || isDouble()) && (other.isInt() || other.isDouble())) {
            return asDouble() == other.asDouble();
        }

        // Handle boolean comparison with numbers (0 = false, non-zero = true)
        if (isBoolean() && (other.isInt() || other.isDouble())) {
            return asBoolean() == (other.asDouble() != 0);
        }

        if ((isInt() || isDouble()) && other.isBoolean()) {
            return (asDouble() != 0) == other.asBoolean();
        }

        return false;
    }

    @Override
    public int hashCode() {
        return value.hashCode();
    }
}