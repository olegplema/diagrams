package org.plema.models;

import java.util.Objects;

public abstract class AbstractBlock implements Visitable {
    protected final Integer id;
    protected AbstractBlock next;
    protected final Integer nextId;

    protected AbstractBlock(AbstractBuilder<?> builder) {
        this.id = builder.id;
        this.next = builder.next;
        this.nextId = builder.nextId;
    }

    public Integer getId() {
        return id;
    }

    public Integer getNextId() {
        return nextId;
    }

    public AbstractBlock getNext() {
        return next;
    }

    public void setNext(AbstractBlock next) {
        this.next = next;
    }

    @Override
    public boolean equals(Object o) {
        if (o == null || getClass() != o.getClass()) return false;
        AbstractBlock that = (AbstractBlock) o;
        return Objects.equals(id, that.id);
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(id);
    }

    public abstract static class AbstractBuilder<T extends AbstractBuilder<T>> {
        private Integer id;
        private AbstractBlock next;
        private Integer nextId;

        @SuppressWarnings("unchecked")
        public T id(Integer id) {
            this.id = id;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T nextId(Integer next) {
            this.nextId = next;
            return (T) this;
        }

        @SuppressWarnings("unchecked")
        public T next(AbstractBlock next) {
            this.next = next;
            return (T) this;
        }

        public abstract AbstractBlock build();
    }
}
