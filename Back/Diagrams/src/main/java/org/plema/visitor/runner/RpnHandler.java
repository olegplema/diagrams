package org.plema.visitor.runner;

import org.plema.DataType;
import org.plema.Value;

import java.util.*;

class RpnHandler {
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

    private static final Set<String> COMPARISON_OPERATORS = Set.of("==", "!=", "<", "<=", ">", ">=");
    private static final Set<String> BOOLEAN_LITERALS = Set.of("true", "false");

    public static Value evaluateExpression(String expression, Map<String, Value> variables) {
        if (BOOLEAN_LITERALS.contains(expression.toLowerCase())) {
            return new Value(Boolean.parseBoolean(expression), DataType.BOOLEAN);
        }

        if (containsLogicalOperators(expression)) {
            return evaluateLogicalExpression(expression, variables);
        }

        if (containsComparisonOperators(expression)) {
            boolean result = evaluateCondition(expression, variables);
            return new Value(result, DataType.BOOLEAN);
        }

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
        expression = evaluateParentheses(expression, variables);

        List<String> tokens = tokenizeLogicalExpression(expression);

        if (tokens.isEmpty()) {
            throw new IllegalArgumentException("Invalid logical expression: " + expression);
        }

        int i = 1;
        while (i < tokens.size()) {
            if (i + 1 >= tokens.size()) {
                throw new IllegalArgumentException("Invalid logical expression format: " + expression);
            }

            String operator = tokens.get(i);
            if ("&&".equals(operator)) {
                boolean leftOperand = evaluateCondition(tokens.get(i - 1), variables);
                boolean rightOperand = evaluateCondition(tokens.get(i + 1), variables);
                boolean result = leftOperand && rightOperand;

                tokens.set(i - 1, String.valueOf(result));
                tokens.remove(i);
                tokens.remove(i);
            } else {
                i += 2;
            }
        }

        boolean result = evaluateCondition(tokens.getFirst(), variables);

        for (i = 1; i < tokens.size(); i += 2) {
            if (i + 1 >= tokens.size()) {
                throw new IllegalArgumentException("Invalid logical expression format: " + expression);
            }

            String operator = tokens.get(i);
            boolean rightOperand = evaluateCondition(tokens.get(i + 1), variables);

            if ("||".equals(operator)) {
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
        int parenCount = 0;

        for (int i = 0; i < expression.length(); i++) {
            char c = expression.charAt(i);

            if (c == '(') {
                parenCount++;
                currentToken.append(c);
            } else if (c == ')') {
                parenCount--;
                currentToken.append(c);
            } else if (parenCount == 0 && i < expression.length() - 1) {
                if ((c == '&' && expression.charAt(i + 1) == '&') ||
                        (c == '|' && expression.charAt(i + 1) == '|')) {

                    if (!currentToken.isEmpty()) {
                        tokens.add(currentToken.toString().trim());
                        currentToken = new StringBuilder();
                    }

                    tokens.add(c + "" + expression.charAt(i + 1));
                    i++;
                } else {
                    currentToken.append(c);
                }
            } else {
                currentToken.append(c);
            }
        }

        if (!currentToken.isEmpty()) {
            tokens.add(currentToken.toString().trim());
        }

        return tokens;
    }

    private static String evaluateParentheses(String expression, Map<String, Value> variables) {
        StringBuilder result = new StringBuilder(expression);

        while (result.toString().contains("(")) {
            int openParenIndex = result.lastIndexOf("(");
            if (openParenIndex == -1) break;

            int closeParenIndex = findMatchingClosingParen(result.toString(), openParenIndex);
            if (closeParenIndex == -1) {
                throw new IllegalArgumentException("Mismatched parentheses in expression: " + expression);
            }

            String subexpression = result.substring(openParenIndex + 1, closeParenIndex);
            Value subexprResult = evaluateLogicalExpression(subexpression, variables);

            result.replace(openParenIndex, closeParenIndex + 1, String.valueOf(subexprResult.value()));
        }

        return result.toString();
    }

    private static int findMatchingClosingParen(String expression, int openParenIndex) {
        int count = 1;
        for (int i = openParenIndex + 1; i < expression.length(); i++) {
            if (expression.charAt(i) == '(') {
                count++;
            } else if (expression.charAt(i) == ')') {
                count--;
                if (count == 0) {
                    return i;
                }
            }
        }
        return -1;
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
        condition = condition.trim();
        if (condition.equalsIgnoreCase("true")) return true;
        if (condition.equalsIgnoreCase("false")) return false;
        if (variables.containsKey(condition) && variables.get(condition).isBoolean()) {
            return (Boolean) variables.get(condition).value();
        }

        if (condition.contains("(")) {
            condition = evaluateParentheses(condition, variables);
        }

        String operator = findOperator(condition);
        if (operator == null) {
            Value result = evaluateExpression(condition, variables);
            if (result.isBoolean()) {
                return (Boolean) result.value();
            }
            throw new IllegalArgumentException("Invalid condition (no operator found): " + condition);
        }

        String[] parts = splitByOperatorRespectingParentheses(condition, operator);
        if (parts.length != 2) {
            throw new IllegalArgumentException("Invalid condition format: " + condition);
        }

        String leftExpr = parts[0].trim();
        String rightExpr = parts[1].trim();

        Value leftValue = getValue(variables, leftExpr);
        Value rightValue = getValue(variables, rightExpr);

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

            return isConditionTrue(operator, leftNum, rightNum);
        } else if (leftValue.isString() && rightValue.isString()) {
            String leftStr = (String) leftValue.value();
            String rightStr = (String) rightValue.value();

            return switch (operator) {
                case "==" -> leftStr.equals(rightStr);
                case "!=" -> !leftStr.equals(rightStr);
                default -> throw new IllegalArgumentException("Invalid operator for string comparison: " + operator);
            };
        } else {
            String leftStr = String.valueOf(leftValue.value());
            String rightStr = String.valueOf(rightValue.value());

            return switch (operator) {
                case "==" -> leftStr.equals(rightStr);
                case "!=" -> !leftStr.equals(rightStr);
                default -> throw new IllegalArgumentException("Invalid operator for mixed type comparison: " + operator);
            };
        }
    }
    private static boolean isConditionTrue(String operator, double leftNum, double rightNum) {
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
    private static String[] splitByOperatorRespectingParentheses(String condition, String operator) {
        int parenCount = 0;
        int operatorPos = -1;

        for (int i = 0; i <= condition.length() - operator.length(); i++) {
            if (condition.charAt(i) == '(') {
                parenCount++;
            } else if (condition.charAt(i) == ')') {
                parenCount--;
            } else if (parenCount == 0 && condition.startsWith(operator, i)) {
                operatorPos = i;
                break;
            }
        }

        if (operatorPos == -1) {
            operatorPos = condition.indexOf(operator);
        }

        if (operatorPos == -1) {
            return new String[0];
        }

        String[] parts = new String[2];
        parts[0] = condition.substring(0, operatorPos);
        parts[1] = condition.substring(operatorPos + operator.length());
        return parts;
    }


    private static Value getValue(Map<String, Value> variables, String expr) {
        expr = expr.trim();
        if (expr.equalsIgnoreCase("true")) {
            return new Value(true, DataType.BOOLEAN);
        } else if (expr.equalsIgnoreCase("false")) {
            return new Value(false, DataType.BOOLEAN);
        }

        if (variables.containsKey(expr)) {
            return variables.get(expr);
        }
        else if (isNumber(expr)) {
            if (isInteger(expr)) {
                return new Value(Integer.parseInt(expr), DataType.INT);
            } else {
                return new Value(Double.parseDouble(expr), DataType.DOUBLE);
            }
        }
        else {
            if (expr.contains("(")) {
                String evaluated = evaluateParentheses(expr, variables);
                if (!evaluated.equals(expr)) {
                    return getValue(variables, evaluated);
                }
            }
            return evaluateExpression(expr, variables);
        }
    }
    private static String findOperator(String condition) {
        if (condition.contains("==")) return "==";
        if (condition.contains("!=")) return "!=";
        if (condition.contains("<=")) return "<=";
        if (condition.contains(">=")) return ">=";
        if (condition.contains("<")) return "<";
        if (condition.contains(">")) return ">";
        return null;
    }

    private static Value applyOperator(Value a, Value b, String operator) {
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

                result = isConditionTrue(operator, aValue, bValue);
            }

            return new Value(result, DataType.BOOLEAN);
        }

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
