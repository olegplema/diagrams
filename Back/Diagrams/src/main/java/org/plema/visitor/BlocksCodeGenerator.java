package org.plema.visitor;

import org.plema.models.*;

import java.util.*;

public class BlocksCodeGenerator implements Visitor {

    private final StringBuilder code;
    private final Map<Integer, AbstractBlock> blockMap;

    private Integer conditionLevel = 0;
    private Integer currentBlockId = 1;
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
        this.code = stringBuilder;
        this.blockMap = blockMap;
    }

    public void addBlock(AbstractBlock block) {
        blockMap.put(block.getId(), block);
    }

    public AbstractBlock getCurrentBlock() {
        return blockMap.get(currentBlockId);
    }

    public boolean hasCurrentBlock() {
        return blockMap.containsKey(currentBlockId);
    }

    public void generateConditionLevel() {
        code.append("    ".repeat(Math.max(0, conditionLevel)));
    }

    @Override
    public void doPrint(PrintBlock printBlock) {
        code.append("                System.out.println(").append(printBlock.getExpression()).append(");\n");
        currentBlockId = printBlock.getNext();
    }

    @Override
    public void doAssign(AssignBlock assignBlock) {
        code.append("                ").append(assignBlock.getExpression()).append(";\n");
        currentBlockId = assignBlock.getNext();
    }

    @Override
    public void doCondition(ConditionBlock conditionBlock) {
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

        currentBlockId = trueBranchId;
    }

    @Override
    public void doWhile(WhileBlock whileBlock) {
        if (!loopBlockIds.contains(whileBlock.getId())) {
            code.append("                while (").append(whileBlock.getExpression()).append(") {\n");
            loopBlockIds.add(whileBlock.getId());
            conditionLevel++;
            currentBlockId = whileBlock.getBody();
        } else {
            conditionLevel--;
            loopBlockIds.remove(whileBlock.getId());
            currentBlockId = whileBlock.getNext();
        }
    }

    @Override
    public void doEnd(EndBlock endBlock) {
        conditionLevel--;
        code.delete(code.length() - 4, code.length());

        if (!conditionStack.isEmpty() && conditionStack.peek().endId.equals(endBlock.getId())) {
            ConditionInfo info = conditionStack.pop();

            if (blockMap.containsKey(info.falseBranchId)) {
                code.append("                } else {\n");
                currentBlockId = info.falseBranchId;
                conditionLevel++;
                return;
            }
            code.append("                }\n");
        } else {
            code.append("                }\n");
        }

        currentBlockId = endBlock.getNext();
    }

    @Override
    public void doInput(InputBlock inputBlock) {
        code.append("                ").append(inputBlock.getVariable().getName()).append(" = scanner.nextInt();\n");
        currentBlockId = inputBlock.getNext();
    }

    @Override
    public void doVariable(Variable variable) {
        code.append("    static ")
                .append(variable.getType())
                .append(" ")
                .append(variable.getName())
                .append(";\n");
    }

    public void finishThread() {
        blockMap.clear();
        currentBlockId = 1;
        conditionLevel = 0;
        conditionStack.clear();
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