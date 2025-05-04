package org.plema.models;

import org.plema.DataType;

public class Variable {
    private final String name;
    private final DataType type;

    public Variable(String name, DataType type) {
        this.name = name;
        this.type = type;
    }

    private Variable(Builder builder) {
        this.name = builder.name;
        this.type = builder.type;
    }

    public String getName() {
        return name;
    }

    public DataType getType() {
        return type;
    }

    public static class Builder {
        private String name;
        private DataType type;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(DataType type) {
            this.type = type;
            return this;
        }

        public Variable build() {
            return new Variable(this);
        }
    }}
