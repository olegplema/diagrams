package org.plema.models;

import org.plema.visitor.Visitor;

public class ConditionBlock extends ExpressionBlock {
    private AbstractBlock trueBranch;
    private AbstractBlock falseBranch;

    private final Integer trueBranchId;
    private final Integer falseBranchId;

    private ConditionBlock(Builder builder) {
        super(builder);
        this.trueBranch = builder.trueBranch;
        this.falseBranch = builder.falseBranch;
        this.trueBranchId = builder.trueBranchId;
        this.falseBranchId = builder.falseBranchId;
    }

    @Override
    public AbstractBlock doVisitor(Visitor v) {
        return v.doCondition(this);
    }

    public AbstractBlock getTrueBranch() {
        return trueBranch;
    }

    public AbstractBlock getFalseBranch() {
        return falseBranch;
    }

    public void setTrueBranch(AbstractBlock trueBranch) {
        this.trueBranch = trueBranch;
    }

    public void setFalseBranch(AbstractBlock falseBranch) {
        this.falseBranch = falseBranch;
    }

    public Integer getTrueBranchId() {
        return trueBranchId;
    }

    public Integer getFalseBranchId() {
        return falseBranchId;
    }

    public static class Builder extends ExpressionBuilder<Builder> {
        private AbstractBlock trueBranch;
        private AbstractBlock falseBranch;

        private Integer trueBranchId;
        private Integer falseBranchId;

        public Builder trueBranch(AbstractBlock trueBranch) {
            this.trueBranch = trueBranch;
            return this;
        }

        public Builder falseBranch(AbstractBlock falseBranch) {
            this.falseBranch = falseBranch;
            return this;
        }

        public Builder trueBranchId(Integer trueBranchId) {
            this.trueBranchId = trueBranchId;
            return this;
        }

        public Builder falseBranchId(Integer falseBranchId) {
            this.falseBranchId = falseBranchId;
            return this;
        }

        @Override
        public ConditionBlock build() {
            return new ConditionBlock(this);
        }
    }
}