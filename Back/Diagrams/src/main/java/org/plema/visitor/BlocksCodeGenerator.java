package org.plema.visitor;

import org.plema.models.*;

import java.util.*;

public class BlocksCodeGenerator implements Visitor {

    private final Map<Integer, AbstractBlock> blockMap;
    private final StringBuilder code;

    private Integer conditionLevel = 0;
    private final Stack<ConditionInfo> conditionStack = new Stack<>();
    private final List<Integer> loopBlockIds = new ArrayList<>();

    private static class ConditionInfo {
        Integer conditionId;
        Integer trueBranchId;
        Integer falseBranchId;
        Integer endId;

        public ConditionInfo(Integer conditionId, Integer trueBranchId, Integer falseBranchId, Integer endId) {
            this.conditionId = conditionId;
            this.trueBranchId = trueBranchId;
            this.falseBranchId = falseBranchId;
            this.endId = endId;
        }
    }

    public BlocksCodeGenerator(StringBuilder stringBuilder, Map<Integer, AbstractBlock> blockMap) {
        this.blockMap = blockMap;
        this.code = stringBuilder;
    }

    @Override
    public Integer doPrint(PrintBlock printBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        code.append("                System.out.println(").append(printBlock.getExpression()).append(");\n");
        return printBlock.getNext();
    }

    @Override
    public Integer doAssign(AssignBlock assignBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        code.append("                ").append(assignBlock.getExpression()).append(";\n");
        return assignBlock.getNext();
    }

    @Override
    public Integer doCondition(ConditionBlock conditionBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        code.append("                if (").append(conditionBlock.getExpression()).append(") {\n");

        conditionLevel++;

        Integer trueBranchId = conditionBlock.getTrueBranch();
        Integer falseBranchId = conditionBlock.getFalseBranch();

        Integer endId = findEndBlockFor(conditionBlock.getId());

        conditionStack.push(new ConditionInfo(
                conditionBlock.getId(),
                trueBranchId,
                falseBranchId,
                endId
        ));

        return trueBranchId;
    }

    @Override
    public Integer doWhile(WhileBlock whileBlock) {
        if (!loopBlockIds.contains(whileBlock.getId())) {
            code.append("    ".repeat(Math.max(0, conditionLevel)));
            code.append("                while (").append(whileBlock.getExpression()).append(") {\n");
            loopBlockIds.add(whileBlock.getId());
            conditionLevel++;
            return whileBlock.getBody();
        } else {
            loopBlockIds.remove(whileBlock.getId());
            return whileBlock.getNext();
        }
    }

    @Override
    public Integer doEnd(EndBlock endBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        conditionLevel--;
        code.delete(code.length() - 4, code.length());

        if (!conditionStack.isEmpty() && conditionStack.peek().endId.equals(endBlock.getId())) {
            ConditionInfo info = conditionStack.pop();

            if (blockMap.containsKey(info.falseBranchId)) {
                code.append("                } else {\n");
                conditionLevel++;
                return info.falseBranchId;
            }
            code.append("                }\n");
        } else {
            code.append("                }\n");
        }

        return endBlock.getNext();
    }

    @Override
    public Integer doInput(InputBlock inputBlock) {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
        code.append("                ").append(inputBlock.getVariable().getName()).append(" = scanner.nextInt();\n");
        return inputBlock.getNext();
    }

    private Integer findEndBlockFor(Integer blockId) {
        AbstractBlock block = blockMap.get(blockId);
        if (block instanceof ConditionBlock conditionBlock) {

            Set<Integer> seenInTrue = new HashSet<>();
            Set<Integer> seenInFalse = new HashSet<>();

            Integer current = conditionBlock.getTrueBranch();
            while (blockMap.containsKey(current) && !seenInTrue.contains(current)) {
                seenInTrue.add(current);
                AbstractBlock currentBlock = blockMap.get(current);
                if (currentBlock instanceof EndBlock) {
                    return current;
                }

                current = currentBlock.getNext();
                if (current == null) break;
            }

            current = conditionBlock.getFalseBranch();
            while (blockMap.containsKey(current) && !seenInFalse.contains(current)) {
                seenInFalse.add(current);
                AbstractBlock currentBlock = blockMap.get(current);
                if (currentBlock instanceof EndBlock) {
                    return current;
                }

                current = currentBlock.getNext();
                if (current == null) break;
            }

            for (Integer id : seenInTrue) {
                if (seenInFalse.contains(id)) {
                    return id;
                }
            }
        } else if (block instanceof WhileBlock whileBlock) {
            return whileBlock.getNext();
        }

        for (Map.Entry<Integer, AbstractBlock> entry : blockMap.entrySet()) {
            if (entry.getValue() instanceof EndBlock && !isAssignedToOtherStructure(entry.getKey(), blockId)) {
                return entry.getKey();
            }
        }

        return null;
    }

    private boolean isAssignedToOtherStructure(Integer endBlockId, Integer excludeBlockId) {
        for (Map.Entry<Integer, AbstractBlock> entry : blockMap.entrySet()) {
            if (!entry.getKey().equals(excludeBlockId)) {
                AbstractBlock block = entry.getValue();
                if (block instanceof WhileBlock && ((WhileBlock) block).getNext().equals(endBlockId)) {
                    return true;
                }
            }
        }
        return false;
    }
}