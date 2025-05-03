package org.plema.models;

public abstract class ExpressionBlock extends AbstractBlock {
    protected final String expression;

    protected ExpressionBlock(ExpressionBuilder<?> builder) {
        super(builder);
        this.expression = builder.expression;
    }

    public String getExpression() {
        return expression;
    }

    public abstract static class ExpressionBuilder<T extends ExpressionBuilder<T>> extends AbstractBuilder<T> {
        private String expression;

        @SuppressWarnings("unchecked")
        public T expression(String expression) {
            this.expression = expression;
            return (T) this;
        }
    }
}