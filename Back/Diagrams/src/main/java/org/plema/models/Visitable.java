package org.plema.models;

import org.plema.visitor.Visitor;

public interface Visitable {
    void doVisitor(Visitor v);
}
