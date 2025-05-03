package org.plema.models;

import org.plema.visitor.Visitor;

public class WhileBlock extends ExpressionBlock {
    private final Integer body;

    private WhileBlock(Builder builder) {
        super(builder);
        this.body = builder.body;
    }

    @Override
    public void doVisitor(Visitor v) {
        v.doWhile(this);
    }

    public Integer getBody() {
        return body;
    }

    public static class Builder extends ExpressionBuilder<Builder> {
        private Integer body;

        public Builder body(Integer body) {
            this.body = body;
            return this;
        }

        @Override
        public WhileBlock build() {
            return new WhileBlock(this);
        }
    }
}