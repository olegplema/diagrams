package org.plema.models;

import org.plema.visitor.Visitor;

public class WhileBlock extends ExpressionBlock {
    private AbstractBlock body;

    private final Integer bodyId;

    private WhileBlock(Builder builder) {
        super(builder);
        this.body = builder.body;
        this.bodyId = builder.bodyId;
    }

    @Override
    public AbstractBlock doVisitor(Visitor v) {
        return v.doWhile(this);
    }

    public AbstractBlock getBody() {
        return body;
    }

    public void setBody(AbstractBlock body) {
        this.body = body;
    }

    public Integer getBodyId() {
        return bodyId;
    }

    public static class Builder extends ExpressionBuilder<Builder> {
        private AbstractBlock body;

        private Integer bodyId;

        public Builder body(AbstractBlock body) {
            this.body = body;
            return this;
        }

        public Builder bodyId(Integer bodyId) {
            this.bodyId = bodyId;
            return this;
        }

        @Override
        public WhileBlock build() {
            return new WhileBlock(this);
        }
    }
}