package org.plema.models;

import org.plema.visitor.Visitor;

public class InputBlock extends AbstractBlock {
    private final Variable variable;

    private InputBlock(Builder builder) {
        super(builder);
        this.variable = builder.variable;
    }

    @Override
    public AbstractBlock doVisitor(Visitor v) {
        return v.doInput(this);
    }

    public Variable getVariable() {
        return variable;
    }

    public static class Builder extends AbstractBuilder<Builder> {
        private Variable variable;

        public Builder variable(Variable variable) {
            if (variable != null) {
                this.variable = variable;
            }
            return this;
        }

        @Override
        public InputBlock build() {
            return new InputBlock(this);
        }
    }}
