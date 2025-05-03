package org.plema.models;

import org.plema.visitor.Visitor;

public class Variable implements Visitable {
    private final String name;
    private final String type;

    public Variable(String name, String type) {
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

    public String getType() {
        return type;
    }

    @Override
    public void doVisitor(Visitor v) {
        v.doVariable(this);
    }

    public static class Builder {
        private String name;
        private String type;

        public Builder name(String name) {
            this.name = name;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Variable build() {
            return new Variable(this);
        }
    }}
