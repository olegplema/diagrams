package org.plema.models;

import org.plema.visitor.Visitor;

public class EndBlock extends AbstractBlock {
    private EndBlock(Builder builder) {
        super(builder);
    }

    @Override
    public AbstractBlock doVisitor(Visitor v) {
        return v.doEnd(this);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        @Override
        public EndBlock build() {
            return new EndBlock(this);
        }
    }
}