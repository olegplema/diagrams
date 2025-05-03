package org.plema.models;

import org.plema.visitor.Visitor;

public class EndBlock extends AbstractBlock {
    private EndBlock(Builder builder) {
        super(builder);
    }

    @Override
    public void doVisitor(Visitor v) {
        v.doEnd(this);
    }

    public static class Builder extends AbstractBuilder<Builder> {
        @Override
        public EndBlock build() {
            return new EndBlock(this);
        }
    }
}