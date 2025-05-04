package org.plema;

public enum DataType {
    STRING("String", ""),
    INT("int", 0),
    DOUBLE("double", 0.0);

    private final String name;
    private final Object defaultValue;

    DataType(String name, Object defaultValue) {
        this.name = name;
        this.defaultValue = defaultValue;
    }

    public String getName() {
        return name;
    }

    public Object getDefaultValue() {
        return defaultValue;
    }
}
