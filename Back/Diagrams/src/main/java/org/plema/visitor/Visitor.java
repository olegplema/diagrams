package org.plema.visitor;

import org.plema.models.*;

public interface Visitor {
    void doPrint(PrintBlock printBlock);
    void doAssign(AssignBlock assignBlock);
    void doCondition(ConditionBlock conditionBlock);
    void doWhile(WhileBlock whileBlock);
    void doEnd(EndBlock endBlock);
    void doInput(InputBlock inputBlock);
    void doVariable(Variable variable);
}
