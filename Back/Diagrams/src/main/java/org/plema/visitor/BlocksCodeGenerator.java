package org.plema.visitor;

import org.plema.DataType;
import org.plema.models.*;

import java.util.*;

public class BlocksCodeGenerator implements Visitor {

    private final Map<Integer, AbstractBlock> blockMap;
    private final StringBuilder code;

    private int conditionLevel = 0;
    private final Stack<ConditionInfo> conditionStack = new Stack<>();
    private final List<WhileBlock> loopBlockIds = new ArrayList<>();

    private static class ConditionInfo {
        AbstractBlock condition;
        AbstractBlock trueBranch;
        AbstractBlock falseBranch;
        AbstractBlock end;

        public ConditionInfo(AbstractBlock condition, AbstractBlock trueBranch, AbstractBlock falseBranch, AbstractBlock end) {
            this.condition = condition;
            this.trueBranch = trueBranch;
            this.falseBranch = falseBranch;
            this.end = end;
        }
    }

    public BlocksCodeGenerator(StringBuilder stringBuilder, Map<Integer, AbstractBlock> blockMap) {
        this.blockMap = blockMap;
        this.code = stringBuilder;
    }

    @Override
    public AbstractBlock doPrint(PrintBlock printBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        code.append("                System.out.println(").append(printBlock.getExpression()).append(");\n");
        return printBlock.getNext();
    }

    @Override
    public AbstractBlock doAssign(AssignBlock assignBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        code.append("                ").append(assignBlock.getExpression()).append(";\n");
        return assignBlock.getNext();
    }

    @Override
    public AbstractBlock doCondition(ConditionBlock conditionBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        code.append("                if (").append(conditionBlock.getExpression()).append(") {\n");

        conditionLevel++;

        AbstractBlock trueBranch = conditionBlock.getTrueBranch();
        AbstractBlock falseBranch = conditionBlock.getFalseBranch();

        AbstractBlock end = findEndBlockFor(conditionBlock);

        conditionStack.push(new ConditionInfo(
                conditionBlock,
                trueBranch,
                falseBranch,
                end
        ));

        return trueBranch;
    }

    @Override
    public AbstractBlock doWhile(WhileBlock whileBlock) {
        if (!loopBlockIds.contains(whileBlock)) {
            code.append("    ".repeat(Math.max(0, conditionLevel)));
            code.append("                while (").append(whileBlock.getExpression()).append(") {\n");
            loopBlockIds.add(whileBlock);
            conditionLevel++;
            return whileBlock.getBody();
        } else {
            loopBlockIds.remove(whileBlock);
            return whileBlock.getNext();
        }
    }

    @Override
    public AbstractBlock doEnd(EndBlock endBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        conditionLevel--;
        code.delete(code.length() - 4, code.length());

        if (!conditionStack.isEmpty() && conditionStack.peek().end.equals(endBlock)) {
            ConditionInfo info = conditionStack.pop();

            if (info.falseBranch != null) {
                code.append("                } else {\n");
                conditionLevel++;
                return info.falseBranch;
            }
            code.append("                }\n");
        } else {
            code.append("                }\n");
        }

        return endBlock.getNext();
    }

    @Override
    public AbstractBlock doInput(InputBlock inputBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        code.append("                ").append(inputBlock.getVariable().getName());
        DataType varType = inputBlock.getVariable().getType();

        switch (varType) {
            case INT -> code.append(" = scanner.nextInt();\n");
            case DOUBLE -> code.append(" = scanner.nextDouble();\n");
            case BOOLEAN -> code.append(" = scanner.nextBoolean();\n");
            case STRING -> code.append(" = scanner.nextLine();\n");
        }

        return inputBlock.getNext();
    }

    private AbstractBlock findEndBlockFor(AbstractBlock block) {
        if (block instanceof ConditionBlock conditionBlock) {

            Set<AbstractBlock> seenInTrue = new HashSet<>();
            Set<AbstractBlock> seenInFalse = new HashSet<>();

            AbstractBlock current = conditionBlock.getTrueBranch();
            while (current != null && !seenInTrue.contains(current)) {
                seenInTrue.add(current);
                AbstractBlock currentBlock = current.getNext();
                if (currentBlock instanceof EndBlock) {
                    return currentBlock;
                }

                current = currentBlock.getNext();
                if (current == null) break;
            }

            current = conditionBlock.getFalseBranch();
            while (current != null && !seenInFalse.contains(current)) {
                seenInFalse.add(current);
                AbstractBlock currentBlock = current.getNext();
                if (currentBlock instanceof EndBlock) {
                    return currentBlock;
                }

                current = currentBlock.getNext();
                if (current == null) break;
            }

            for (AbstractBlock id : seenInTrue) {
                if (seenInFalse.contains(id)) {
                    return id;
                }
            }
        } else if (block instanceof WhileBlock whileBlock) {
            return whileBlock.getNext();
        }

        for (Map.Entry<Integer, AbstractBlock> entry : blockMap.entrySet()) {
            AbstractBlock entryBlock = entry.getValue();
            if (entryBlock instanceof EndBlock && !isAssignedToOtherStructure(entryBlock, block)) {
                return entryBlock;
            }
        }
        return null;
    }

    private boolean isAssignedToOtherStructure(AbstractBlock endBlock, AbstractBlock excludeBlock) {
        for (AbstractBlock block : blockMap.values()) {
            if (block != excludeBlock) {
                if (block instanceof WhileBlock whileBlock && whileBlock.getNext() == endBlock) {
                    return true;
                }
            }
        }
        return false;
    }}