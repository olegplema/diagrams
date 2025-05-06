package org.plema.visitor.runner;

import org.plema.DataType;
import org.plema.Value;
import org.plema.models.*;
import org.plema.visitor.Visitor;

import java.util.Map;

public class BlocksCodeRunner implements Visitor {
    private final Map<String, Value> variables;

    public BlocksCodeRunner(Map<String, Value> variables) {
        this.variables = variables;
    }

    @Override
    public Integer doPrint(PrintBlock printBlock) {
        String expression = printBlock.getExpression();
        String evaluated = replaceVariablesWithValues(expression, variables);
        System.out.println(evaluated);
        return printBlock.getNext();
    }

    @Override
    public Integer doAssign(AssignBlock assignBlock) {
        String[] parts = assignBlock.getExpression().split("=", 2);
        String varName = parts[0].trim();
        if (!variables.containsKey(varName)) {
            throw new IllegalArgumentException("Variable " + varName + " not found");
        }
        Value value = RpnHandler.evaluateExpression(parts[1].trim(), variables);
        variables.put(varName, value);
        return assignBlock.getNext();
    }

    @Override
    public Integer doCondition(ConditionBlock conditionBlock) {
        String condition = conditionBlock.getExpression();
        boolean result = RpnHandler.evaluateCondition(condition, variables);
        return result ? conditionBlock.getTrueBranch() : conditionBlock.getFalseBranch();
    }

    @Override
    public Integer doWhile(WhileBlock whileBlock) {
        String condition = whileBlock.getExpression();
        if (RpnHandler.evaluateCondition(condition, variables)) {
            return whileBlock.getBody();
        } else {
            return whileBlock.getNext();
        }
    }

    @Override
    public Integer doEnd(EndBlock endBlock) {
        return endBlock.getNext();
    }

    @Override
    public Integer doInput(InputBlock inputBlock) {
        String varName = inputBlock.getVariable().getName();
        String input = "5"; // use WebSocket TODO
        Value value = isInteger(input) ? new Value(Integer.parseInt(input), DataType.INT) :
                isDouble(input) ? new Value(Double.parseDouble(input), DataType.DOUBLE) :
                        new Value(input, DataType.STRING);
        variables.put(varName, value);
        return inputBlock.getNext();
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
