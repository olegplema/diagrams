package org.plema.services;

import org.plema.DataType;
import org.plema.Value;

import java.util.*;

public class ExpressionService {
    private static final Map<String, Integer> OPERATOR_PRECEDENCE = Map.of(
            "+", 1, "-", 1,
            "*", 2, "/", 2
    );

    public static Value evaluateExpression(String expression, Map<String, Value> variables) {
        for (Map.Entry<String, Value> entry : variables.entrySet()) {
            expression = expression.replaceAll("\\b" + entry.getKey() + "\\b", entry.getValue().value().toString());
        }

        List<String> rpn = toReversePolishNotation(expression, variables);
        return evaluateRPN(rpn, variables);
    }

    private static List<String> toReversePolishNotation(String expression, Map<String, Value> variables) {
        List<String> output = new ArrayList<>();
        Deque<String> operators = new ArrayDeque<>();

        StringTokenizer tokenizer = new StringTokenizer(expression, "+-*/()", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (token.isEmpty()) continue;

            if (isNumber(token) || variables.containsKey(token)) {
                output.add(token);
            } else if (OPERATOR_PRECEDENCE.containsKey(token)) {
                while (!operators.isEmpty() && !operators.peek().equals("(") &&
                        OPERATOR_PRECEDENCE.getOrDefault(operators.peek(), 0) >= OPERATOR_PRECEDENCE.get(token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                if (!operators.isEmpty() && operators.peek().equals("(")) {
                    operators.pop();
                }
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        return output;
    }

    private static Value evaluateRPN(List<String> rpn, Map<String, Value> variables) {
        Deque<Value> stack = new ArrayDeque<>();

        for (String token : rpn) {
            if (isNumber(token)) {
                stack.push(new Value(Double.parseDouble(token), DataType.DOUBLE));
            } else if (variables.containsKey(token)) {
                stack.push(variables.get(token));
            } else if (OPERATOR_PRECEDENCE.containsKey(token)) {
                Value b = stack.pop();
                Value a = stack.pop();
                stack.push(applyOperator(a, b, token));
            } else {
                throw new IllegalArgumentException("Unknown token: " + token);
            }
        }

        return stack.pop();
    }

    public static boolean evaluateCondition(String condition, Map<String, Value> variables) {
        String operator = findOperator(condition);
        if (operator == null) {
            throw new IllegalArgumentException("Invalid condition (no operator found): " + condition);
        }

        String[] parts = condition.split(escapeRegex(operator), 2);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid condition format: " + condition);
        }

        String leftExpr = parts[0].trim();
        String rightExpr = parts[1].trim();

        Value leftValue = getValue(variables, leftExpr);

        Value rightValue = getValue(variables, rightExpr);

        double leftNum = leftValue.asDouble();
        double rightNum = rightValue.asDouble();

        return switch (operator) {
            case "==" -> leftNum == rightNum;
            case "!=" -> leftNum != rightNum;
            case "<" -> leftNum < rightNum;
            case "<=" -> leftNum <= rightNum;
            case ">" -> leftNum > rightNum;
            case ">=" -> leftNum >= rightNum;
            default -> throw new IllegalArgumentException("Invalid operator: " + operator);
        };
    }

    private static Value getValue(Map<String, Value> variables, String leftExpr) {
        Value leftValue;
        if (variables.containsKey(leftExpr)) {
            leftValue = variables.get(leftExpr);
        } else if (isNumber(leftExpr)) {
            leftValue = isInteger(leftExpr) ?
                    new Value(Integer.parseInt(leftExpr), DataType.INT) :
                    new Value(Double.parseDouble(leftExpr), DataType.DOUBLE);
        } else {
            // Try to evaluate as an expression
            leftValue = evaluateExpression(leftExpr, variables);
        }
        return leftValue;
    }

    private static String findOperator(String condition) {
        if (condition.contains("<=")) return "<=";
        if (condition.contains(">=")) return ">=";
        if (condition.contains("==")) return "==";
        if (condition.contains("!=")) return "!=";
        if (condition.contains("<")) return "<";
        if (condition.contains(">")) return ">";
        return null;
    }

    private static String escapeRegex(String operator) {
        // Escape special regex characters
        return operator.replaceAll("([\\[\\]{}()*+?.\\\\^$|])", "\\\\$1");
    }

    private static Value applyOperator(Value a, Value b, String operator) {
        if (a.isInt() && b.isInt()) {
            int result;
            switch (operator) {
                case "+" -> result = a.asInt() + b.asInt();
                case "-" -> result = a.asInt() - b.asInt();
                case "*" -> result = a.asInt() * b.asInt();
                case "/" -> result = a.asInt() / b.asInt();
                default -> throw new IllegalArgumentException("Unknown operator: " + operator);
            }
            return new Value(result, DataType.INT);
        } else {
            double result;
            switch (operator) {
                case "+" -> result = a.asDouble() + b.asDouble();
                case "-" -> result = a.asDouble() - b.asDouble();
                case "*" -> result = a.asDouble() * b.asDouble();
                case "/" -> result = a.asDouble() / b.asDouble();
                default -> throw new IllegalArgumentException("Unknown operator: " + operator);
            }
            return new Value(result, DataType.DOUBLE);
        }
    }

    private static boolean isNumber(String str) {
        try {
            Double.parseDouble(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }

    private static boolean isInteger(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
