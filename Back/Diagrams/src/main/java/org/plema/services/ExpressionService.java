package org.plema.services;

import org.plema.DataType;
import org.plema.Value;

import java.util.*;

public class ExpressionService {
    private static final Map<String, Integer> OPERATOR_PRECEDENCE = Map.ofEntries(
            Map.entry("+", 1),
            Map.entry("-", 1),
            Map.entry("*", 2),
            Map.entry("/", 2),
            Map.entry("&&", 0),
            Map.entry("||", 0),
            Map.entry("==", 0),
            Map.entry("!=", 0),
            Map.entry("<", 0),
            Map.entry("<=", 0),
            Map.entry(">", 0),
            Map.entry(">=", 0)
    );

    private static final Set<String> LOGICAL_OPERATORS = Set.of("&&", "||");
    private static final Set<String> COMPARISON_OPERATORS = Set.of("==", "!=", "<", "<=", ">", ">=");
    private static final Set<String> BOOLEAN_LITERALS = Set.of("true", "false");

    public static Value evaluateExpression(String expression, Map<String, Value> variables) {
        // Handle direct boolean assignment like X = true
        if (BOOLEAN_LITERALS.contains(expression.toLowerCase())) {
            return new Value(Boolean.parseBoolean(expression), DataType.BOOLEAN);
        }

        // Check for logical expressions (containing && or ||)
        if (containsLogicalOperators(expression)) {
            return evaluateLogicalExpression(expression, variables);
        }

        // Check for comparison expressions (containing ==, !=, <, <=, >, >=)
        if (containsComparisonOperators(expression)) {
            boolean result = evaluateCondition(expression, variables);
            return new Value(result, DataType.BOOLEAN);
        }

        // Continue with the original arithmetic expression evaluation
        for (Map.Entry<String, Value> entry : variables.entrySet()) {
            expression = expression.replaceAll("\\b" + entry.getKey() + "\\b",
                    entry.getValue().value().toString());
        }

        List<String> rpn = toReversePolishNotation(expression, variables);
        return evaluateRPN(rpn, variables);
    }

    private static boolean containsLogicalOperators(String expression) {
        return expression.contains("&&") || expression.contains("||");
    }

    private static boolean containsComparisonOperators(String expression) {
        return expression.contains("==") || expression.contains("!=") ||
                expression.contains("<=") || expression.contains(">=") ||
                expression.contains("<") || expression.contains(">");
    }

    private static Value evaluateLogicalExpression(String expression, Map<String, Value> variables) {
        // Split by logical operators while preserving them
        List<String> tokens = tokenizeLogicalExpression(expression);

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Invalid logical expression: " + expression);
        }

        // Evaluate first comparison
        boolean result = evaluateCondition(tokens.get(0), variables);

        // Process remaining operators and comparisons
        for (int i = 1; i < tokens.size(); i += 2) {
            if (i + 1 >= tokens.size()) {
                throw new IllegalArgumentException("Invalid logical expression format: " + expression);
            }

            String operator = tokens.get(i);
            boolean rightOperand = evaluateCondition(tokens.get(i + 1), variables);

            if ("&&".equals(operator)) {
                result = result && rightOperand;
            } else if ("||".equals(operator)) {
                result = result || rightOperand;
            } else {
                throw new IllegalArgumentException("Unknown logical operator: " + operator);
            }
        }

        return new Value(result, DataType.BOOLEAN);
    }

    private static List<String> tokenizeLogicalExpression(String expression) {
        List<String> tokens = new ArrayList<>();
        StringBuilder currentToken = new StringBuilder();

        int i = 0;
        while (i < expression.length()) {
            // Check for logical operators
            if (i + 1 < expression.length()) {
                String twoChars = expression.substring(i, i + 2);
                if ("&&".equals(twoChars) || "||".equals(twoChars)) {
                    if (currentToken.length() > 0) {
                        tokens.add(currentToken.toString().trim());
                        currentToken = new StringBuilder();
                    }
                    tokens.add(twoChars);
                    i += 2;
                    continue;
                }
            }

            currentToken.append(expression.charAt(i));
            i++;
        }

        if (currentToken.length() > 0) {
            tokens.add(currentToken.toString().trim());
        }

        return tokens;
    }

    private static List<String> toReversePolishNotation(String expression, Map<String, Value> variables) {
        List<String> output = new ArrayList<>();
        Deque<String> operators = new ArrayDeque<>();

        StringTokenizer tokenizer = new StringTokenizer(expression, "+-*/()", true);
        while (tokenizer.hasMoreTokens()) {
            String token = tokenizer.nextToken().trim();
            if (token.isEmpty()) continue;

            if (isNumber(token) || variables.containsKey(token) || BOOLEAN_LITERALS.contains(token.toLowerCase())) {
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
                if (isInteger(token)) {
                    stack.push(new Value(Integer.parseInt(token), DataType.INT));
                } else {
                    stack.push(new Value(Double.parseDouble(token), DataType.DOUBLE));
                }
            } else if (BOOLEAN_LITERALS.contains(token.toLowerCase())) {
                stack.push(new Value(Boolean.parseBoolean(token), DataType.BOOLEAN));
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
        // Handle direct boolean values or variables
        if (condition.trim().equalsIgnoreCase("true")) return true;
        if (condition.trim().equalsIgnoreCase("false")) return false;
        if (variables.containsKey(condition.trim()) && variables.get(condition.trim()).isBoolean()) {
            return (Boolean) variables.get(condition.trim()).value();
        }

        String operator = findOperator(condition);
        if (operator == null) {
            // Try to evaluate as an expression that might result in a boolean
            Value result = evaluateExpression(condition, variables);
            if (result.isBoolean()) {
                return (Boolean) result.value();
            }
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

        // Handle different type comparisons
        if (leftValue.isBoolean() && rightValue.isBoolean()) {
            boolean leftBool = (Boolean) leftValue.value();
            boolean rightBool = (Boolean) rightValue.value();

            return switch (operator) {
                case "==" -> leftBool == rightBool;
                case "!=" -> leftBool != rightBool;
                default -> throw new IllegalArgumentException("Invalid operator for boolean comparison: " + operator);
            };
        } else if ((leftValue.isInt() || leftValue.isDouble()) &&
                (rightValue.isInt() || rightValue.isDouble())) {
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
        } else if (leftValue.isString() && rightValue.isString()) {
            String leftStr = (String) leftValue.value();
            String rightStr = (String) rightValue.value();

            return switch (operator) {
                case "==" -> leftStr.equals(rightStr);
                case "!=" -> !leftStr.equals(rightStr);
                default -> throw new IllegalArgumentException("Invalid operator for string comparison: " + operator);
            };
        } else {
            // Compare string representations for mixed types
            String leftStr = String.valueOf(leftValue.value());
            String rightStr = String.valueOf(rightValue.value());

            return switch (operator) {
                case "==" -> leftStr.equals(rightStr);
                case "!=" -> !leftStr.equals(rightStr);
                default -> throw new IllegalArgumentException("Invalid operator for mixed type comparison: " + operator);
            };
        }
    }

    private static Value getValue(Map<String, Value> variables, String expr) {
        // Handle boolean literals directly
        if (expr.equalsIgnoreCase("true")) {
            return new Value(true, DataType.BOOLEAN);
        } else if (expr.equalsIgnoreCase("false")) {
            return new Value(false, DataType.BOOLEAN);
        }

        // Check if it's a variable
        if (variables.containsKey(expr)) {
            return variables.get(expr);
        }
        // Check if it's a number
        else if (isNumber(expr)) {
            if (isInteger(expr)) {
                return new Value(Integer.parseInt(expr), DataType.INT);
            } else {
                return new Value(Double.parseDouble(expr), DataType.DOUBLE);
            }
        }
        // Try to evaluate as an expression
        else {
            return evaluateExpression(expr, variables);
        }
    }

    private static String findOperator(String condition) {
        // Check for comparison operators in priority order (longer ones first)
        if (condition.contains("==")) return "==";
        if (condition.contains("!=")) return "!=";
        if (condition.contains("<=")) return "<=";
        if (condition.contains(">=")) return ">=";
        if (condition.contains("<")) return "<";
        if (condition.contains(">")) return ">";
        return null;
    }

    private static String escapeRegex(String operator) {
        return operator.replaceAll("([\\[\\]{}()*+?.\\\\^$|])", "\\\\$1");
    }

    private static Value applyOperator(Value a, Value b, String operator) {
        // Handle boolean operations
        if (operator.equals("&&") || operator.equals("||")) {
            boolean result;
            boolean aValue = a.asBoolean();
            boolean bValue = b.asBoolean();

            if (operator.equals("&&")) {
                result = aValue && bValue;
            } else {
                result = aValue || bValue;
            }
            return new Value(result, DataType.BOOLEAN);
        }

        // Handle comparison operations
        if (COMPARISON_OPERATORS.contains(operator)) {
            boolean result;

            if (a.isBoolean() && b.isBoolean()) {
                boolean aValue = a.asBoolean();
                boolean bValue = b.asBoolean();

                result = switch (operator) {
                    case "==" -> aValue == bValue;
                    case "!=" -> aValue != bValue;
                    default -> throw new IllegalArgumentException("Invalid operator for boolean comparison: " + operator);
                };
            } else {
                double aValue = a.asDouble();
                double bValue = b.asDouble();

                result = switch (operator) {
                    case "==" -> aValue == bValue;
                    case "!=" -> aValue != bValue;
                    case "<" -> aValue < bValue;
                    case "<=" -> aValue <= bValue;
                    case ">" -> aValue > bValue;
                    case ">=" -> aValue >= bValue;
                    default -> throw new IllegalArgumentException("Invalid comparison operator: " + operator);
                };
            }

            return new Value(result, DataType.BOOLEAN);
        }

        // Handle arithmetic operations
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