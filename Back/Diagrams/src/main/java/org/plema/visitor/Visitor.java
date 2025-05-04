package org.plema.visitor;

import org.plema.models.*;

public interface Visitor {
    Integer doPrint(PrintBlock printBlock);
    Integer doAssign(AssignBlock assignBlock);
    Integer doCondition(ConditionBlock conditionBlock);
    Integer doWhile(WhileBlock whileBlock);
    Integer doEnd(EndBlock endBlock);
    Integer doInput(InputBlock inputBlock);
}
