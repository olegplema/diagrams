package org.plema.services;

import org.plema.models.AbstractBlock;
import org.plema.models.Diagram;
import org.plema.models.Variable;
import org.plema.visitor.BlocksCodeGenerator;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class DiagramService {

    public String generateCode(Diagram diagram) {
        StringBuilder code = new StringBuilder();
        Map<Integer, AbstractBlock> blockMap = new HashMap<>();
        BlocksCodeGenerator visitor = new BlocksCodeGenerator(code, blockMap);

        code.append("import java.util.Scanner;\n");
        code.append("import java.util.concurrent.locks.ReentrantLock;\n\n");
        code.append("public class FlowchartThreads {\n");

        for (Variable variable : diagram.variables()) {
            variable.doVisitor(visitor);
        }

        code.append("    static final Scanner scanner = new Scanner(System.in);\n");
        code.append("    static final ReentrantLock lock = new ReentrantLock();\n\n");

        Map<Integer, String> generatedThreads = new HashMap<>();
        for (int i = 0; i < diagram.threads().size(); i++) {
            var thread = diagram.threads().get(i);
            String threadName = "Task" + (i + 1);
            generatedThreads.put(i + 1, threadName);
            code.append("    static class ").append(threadName).append(" extends Thread {\n");
            code.append("        public void run() {\n");
            code.append("            try {\n");
            generateBlocks(thread, visitor);
            code.append("            } catch (Throwable t) {\n");
            code.append("                System.err.println(t);\n");
            code.append("            }\n");
            code.append("        }\n");
            code.append("    }\n\n");
        }

        code.append("    public static void main(String[] args) {\n");
        int counter = 1;
        for (String thread : generatedThreads.values()) {
            code.append("        Thread t").append(counter).append(" = new ").append(thread).append("();\n");
            code.append("        t").append(counter).append(".start();\n");
            counter++;
        }
        code.append("    }\n}\n");

        return code.toString();
    }

    private void generateBlocks(List<AbstractBlock> blocks, BlocksCodeGenerator blocksCodeGenerator) {
        for (AbstractBlock block : blocks) {
            blocksCodeGenerator.addBlock(block);
        }

        while (blocksCodeGenerator.hasCurrentBlock()) {
            AbstractBlock block = blocksCodeGenerator.getCurrentBlock();
            blocksCodeGenerator.generateConditionLevel();
            block.doVisitor(blocksCodeGenerator);
        }

        blocksCodeGenerator.finishThread();
    }
}
