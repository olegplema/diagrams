package org.plema.models;

import org.plema.visitor.Visitor;

public class AssignBlock extends ExpressionBlock{
    private AssignBlock(Builder builder) {
        super(builder);
    }

    @Override
    public Integer doVisitor(Visitor v) {
        return v.doAssign(this);
    }

    public static class Builder extends ExpressionBuilder<Builder> {
        @Override
        public AssignBlock build() {
            return new AssignBlock(this);
        }
    }
}
