package org.plema.services;

import org.plema.Value;

import java.util.*;

public class ExpressionService {
    private static final Map<String, Integer> OPERATOR_PRECEDENCE = Map.of(
            "+", 1, "-", 1,
            "*", 2, "/", 2
    );

    public static Value evaluateExpression(String expression, Map<String, Value> variables) {
        List<String> rpn = toReversePolishNotation(expression, variables);
        return evaluateRPN(rpn);
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
                while (!operators.isEmpty() && OPERATOR_PRECEDENCE.getOrDefault(operators.peek(), 0) >= OPERATOR_PRECEDENCE.get(token)) {
                    output.add(operators.pop());
                }
                operators.push(token);
            } else if (token.equals("(")) {
                operators.push(token);
            } else if (token.equals(")")) {
                while (!operators.isEmpty() && !operators.peek().equals("(")) {
                    output.add(operators.pop());
                }
                operators.pop();
            }
        }

        while (!operators.isEmpty()) {
            output.add(operators.pop());
        }

        return output;
    }

    private static Value evaluateRPN(List<String> rpn) {
        Deque<Value> stack = new ArrayDeque<>();

        for (String token : rpn) {
            if (isNumber(token)) {
                stack.push(new Value(Double.parseDouble(token), "double"));
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
        String[] parts = condition.split("(<=|>=|==|!=|<|>)");
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid condition: " + condition);
        }
        String left = parts[0].trim();
        String right = parts[1].trim();
        String operator = condition.replace(left, "").replace(right, "").trim();

        Value leftValue = variables.getOrDefault(left, new Value(Double.parseDouble(left),"double"));
        Value rightValue = variables.getOrDefault(right, new Value(Double.parseDouble(right),"double"));

        double leftNum = Double.parseDouble(leftValue.value().toString());
        double rightNum = Double.parseDouble(rightValue.value().toString());

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
            return new Value(result, "int");
        } else {
            double result;
            switch (operator) {
                case "+" -> result = a.asDouble() + b.asDouble();
                case "-" -> result = a.asDouble() - b.asDouble();
                case "*" -> result = a.asDouble() * b.asDouble();
                case "/" -> result = a.asDouble() / b.asDouble();
                default -> throw new IllegalArgumentException("Unknown operator: " + operator);
            }
            return new Value(result, "double");
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
}