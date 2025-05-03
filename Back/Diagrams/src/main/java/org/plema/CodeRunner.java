package org.plema;

import com.fasterxml.jackson.databind.JsonNode;
import org.plema.services.ExpressionService;

import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class CodeRunner {
    public void runBlocks(JsonNode blocks) {
        Map<String, Value> variables = new HashMap<>();
        Map<Integer, JsonNode> blockMap = new HashMap<>();
        for (JsonNode block : blocks) {
            blockMap.put(block.get("id").asInt(), block);
        }
        int currentBlockId = 1;
        Scanner scanner = new Scanner(System.in);

        while (blockMap.containsKey(currentBlockId)) {
            JsonNode block = blockMap.get(currentBlockId);
            switch (BlockType.valueOf(block.get("type").asText())) {
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
                    String varName = block.get("varName").asText();
                    System.out.print("Enter value for " + varName + ": ");
                    String input = scanner.nextLine();
                    Value value = isInteger(input) ? new Value(Integer.parseInt(input), "int") :
                            isDouble(input) ? new Value(Double.parseDouble(input), "double") :
                                    new Value("string", input);
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
        String regex = "(?<!\")\\b[A-Za-z_][A-Za-z0-9_]*\\b(?!\")";

        Pattern pattern = Pattern.compile(regex);
        Matcher matcher = pattern.matcher(expression);

        StringBuilder result = new StringBuilder();

        while (matcher.find()) {
            String variableName = matcher.group();
            Value variableValue = variables.get(variableName);

            if (variableValue == null) {
                throw new IllegalArgumentException("Variable " + variableName + " not found");
            }

            String replacement = getValueAsString(variableValue);
            matcher.appendReplacement(result, replacement);
        }

        matcher.appendTail(result);
        return result.toString();
    }

    private String getValueAsString(Value value) {
        return switch (value.type()) {
            case "int", "double" -> String.valueOf(value.value());
            case "string" -> "\"" + value.value() + "\"";
            default -> throw new IllegalArgumentException("Unknown value type: " + value.value());
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

    private static boolean isString(String str) {
        return !isInteger(str) && !isDouble(str);
    }
}
