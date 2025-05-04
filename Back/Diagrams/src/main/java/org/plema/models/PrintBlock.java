package org.plema.models;

import org.plema.visitor.Visitor;

public class PrintBlock extends ExpressionBlock {
    private PrintBlock(Builder builder) {
        super(builder);
    }

    @Override
    public Integer doVisitor(Visitor v) {
        return v.doPrint(this);
    }

    public static class Builder extends ExpressionBuilder<Builder> {
        @Override
        public PrintBlock build() {
            return new PrintBlock(this);
        }
    }
}