package org.plema.visitor;

import org.plema.models.*;

import java.util.Map;

public class BlocksCodeGenerator implements Visitor {

    private final StringBuilder code;
    private final Map<Integer, AbstractBlock> blockMap;

    private Integer conditionLevel = 0;
    private Integer currentBlockId = 1;

    public BlocksCodeGenerator(StringBuilder stringBuilder, Map<Integer, AbstractBlock> blockMap ) {
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

        int trueBranchId = conditionBlock.getTrueBranch();
        int falseBranchId = conditionBlock.getFalseBranch();

        if (blockMap.containsKey(trueBranchId)) {
            currentBlockId = trueBranchId;
            return;
        }

        if (blockMap.containsKey(falseBranchId) && !isEndBlock(falseBranchId)) {
            code.append("                } else {\n");
            currentBlockId = falseBranchId;
        } else {
            code.append("                }\n");
        }

    }

    @Override
    public void doWhile(WhileBlock whileBlock) {
        code.append("                while (").append(whileBlock.getExpression()).append(") {\n");
        conditionLevel++;
        currentBlockId = whileBlock.getBody();
    }

    @Override
    public void doEnd(EndBlock endBlock) {
        code.delete(code.length() - 4, code.length());
        code.append("                }\n");
        currentBlockId = endBlock.getNext();
        conditionLevel--;
    }

    @Override
    public void doInput(InputBlock inputBlock) {
        code.append("                ").append(inputBlock.getVariable()).append(" = scanner.nextInt();\n");
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

    private boolean isEndBlock(int blockId) {
        return !blockMap.containsKey(blockId) || blockMap.get(blockId) instanceof EndBlock;
    }

}
