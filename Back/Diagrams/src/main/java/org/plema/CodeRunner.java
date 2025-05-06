package org.plema;

import com.fasterxml.jackson.databind.JsonNode;
import org.plema.services.ExpressionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;

public class CodeRunner {
    public void runBlocks(JsonNode blocks) {
        Map<String, Value> variables = new HashMap<>();
        Map<Integer, JsonNode> blockMap = new HashMap<>();
        for (JsonNode block : blocks) {
            blockMap.put(block.get("id").asInt(), block);
        }
        int currentBlockId = 1;

        while (blockMap.containsKey(currentBlockId)) {
            JsonNode block = blockMap.get(currentBlockId);
            switch (BlockType.valueOf(block.get("type").asText().toUpperCase())) {
                case ASSIGN -> {
                    String expression = block.get("expression").asText();
                    String[] parts = expression.split("=", 2);
                    String varName = parts[0].trim();
                    Value value = ExpressionService.evaluateExpression(parts[1].trim(), variables);
                    variables.put(varName, value);
                    currentBlockId = block.get("next").asInt();
                }
                case PRINT -> {
                    String expression = block.get("expression").asText();
                    String evaluated = replaceVariablesWithValues(expression, variables);
                    System.out.println(evaluated);
                    currentBlockId = block.get("next").asInt();
                }
                case INPUT -> {
                    String varName = block.get("variable").asText();
                    System.out.println("Enter value for " + varName + ": ");
                    String input = "5";//scanner.nextLine();
                    Value value = isInteger(input) ? new Value(Integer.parseInt(input), DataType.INT) :
                            isDouble(input) ? new Value(Double.parseDouble(input), DataType.DOUBLE) :
                                    new Value(input, DataType.STRING);
                    variables.put(varName, value);
                    currentBlockId = block.get("next").asInt();
                }
                case CONDITION -> {
                    String condition = block.get("expression").asText();
                    boolean result = ExpressionService.evaluateCondition(condition, variables);
                    currentBlockId = result ? block.get("trueBranch").asInt() : block.get("falseBranch").asInt();
                }
                case END_CONDITION, END -> {
                    currentBlockId = block.has("next") ? block.get("next").asInt() : -1;
                }
                case WHILE -> {
                    String condition = block.get("expression").asText();
                    if (ExpressionService.evaluateCondition(condition, variables)) {
                        currentBlockId = block.get("body").asInt();
                    } else {
                        currentBlockId = block.get("next").asInt();
                    }
                }
            }
        }
    }

    private String replaceVariablesWithValues(String expression, Map<String, Value> variables) {
        StringBuilder result = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (c == '"') {
                inQuotes = !inQuotes;
                result.append(c);
                continue;
            }

            if (inQuotes) {
                result.append(c);
                continue;
            }

            if (Character.isLetterOrDigit(c) || c == '_') {
                if (i == 0 || !(Character.isLetterOrDigit(expression.charAt(i-1)) || expression.charAt(i-1) == '_')) {
                    int start = i;

                    while (i < expression.length() &&
                            (Character.isLetterOrDigit(expression.charAt(i)) || expression.charAt(i) == '_')) {
                        i++;
                    }

                    String variableName = expression.substring(start, i);
                    i--;

                    Value variableValue = variables.get(variableName);
                    if (variableValue != null) {
                        result.append(getValueAsString(variableValue));
                    } else {
                        result.append(variableName);
                    }
                } else {
                    result.append(c);
                }
            } else {
                result.append(c);
            }
        }

        return result.toString();
    }

    private String getValueAsString(Value value) {
        return switch (value.type()) {
            case DataType.INT, DataType.DOUBLE, DataType.BOOLEAN -> String.valueOf(value.value());
            case DataType.STRING -> "\"" + value.value() + "\"";
        };
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isDouble(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
