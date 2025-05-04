package org.plema.models;

import org.plema.visitor.Visitor;

public class ConditionBlock extends ExpressionBlock {
    private final Integer trueBranch;
    private final Integer falseBranch;

    private ConditionBlock(Builder builder) {
        super(builder);
        this.trueBranch = builder.trueBranch;
        this.falseBranch = builder.falseBranch;
    }

    @Override
    public Integer doVisitor(Visitor v) {
        return v.doCondition(this);
    }

    public Integer getTrueBranch() {
        return trueBranch;
    }

    public Integer getFalseBranch() {
        return falseBranch;
    }

    public static class Builder extends ExpressionBuilder<Builder> {
        private Integer trueBranch;
        private Integer falseBranch;

        public Builder trueBranch(Integer trueBranch) {
            this.trueBranch = trueBranch;
            return this;
        }

        public Builder falseBranch(Integer falseBranch) {
            this.falseBranch = falseBranch;
            return this;
        }

        @Override
        public ConditionBlock build() {
            return new ConditionBlock(this);
        }
    }
}