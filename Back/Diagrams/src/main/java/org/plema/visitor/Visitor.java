package org.plema.visitor;

import org.plema.models.*;

public interface Visitor {
    AbstractBlock doPrint(PrintBlock printBlock);
    AbstractBlock doAssign(AssignBlock assignBlock);
    AbstractBlock doCondition(ConditionBlock conditionBlock);
    AbstractBlock doWhile(WhileBlock whileBlock);
    AbstractBlock doEnd(EndBlock endBlock);
    AbstractBlock doInput(InputBlock inputBlock);
}
