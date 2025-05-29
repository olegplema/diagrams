package org.plema.models;

import org.plema.visitor.Visitor;

public interface Visitable {
    AbstractBlock doVisitor(Visitor v);
}
