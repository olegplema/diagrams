package org.plema.models;

public abstract class AbstractBlock implements Visitable {
    protected final Integer id;
    protected final Integer next;

    protected AbstractBlock(AbstractBuilder<?> builder) {
        this.id = builder.id;
        this.next = builder.next;
    }

    public Integer getId() {
        return id;
    }

    public Integer getNext() {
        return next;
    }

    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private Integer id;
        private Integer next;

        @SuppressWarnings("unchecked")
        public T id(Integer id) {
            this.id = id;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T next(Integer next) {
            this.next = next;
            return (T) this;
        }

        public abstract AbstractBlock build();
    }}
