package org.plema.models;

import org.plema.visitor.Visitor;

public interface Visitable {
    Integer doVisitor(Visitor v);
}
