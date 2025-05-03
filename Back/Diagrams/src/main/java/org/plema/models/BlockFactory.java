package org.plema.models;

import io.vertx.core.json.JsonObject;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;

public class BlockFactory {
    private final Map<String, Function<JsonObject, AbstractBlock>> blockBuilders = new HashMap<>();
    private final Map<String, Variable> variableMap = new HashMap<>();

    public BlockFactory(List<Variable> variables) {
        for (Variable variable : variables) {
            variableMap.put(variable.getName(), variable);
        }

        registerBlockType("input", json -> {
            String variableName = json.getString("variable");
            return new InputBlock.Builder()
                .id(json.getInteger("id"))
                .next(json.getInteger("next"))
                .variable(variableMap.get(variableName))
                .build();
        });

        registerBlockType("assign", json -> new AssignBlock.Builder()
                .id(json.getInteger("id"))
                .next(json.getInteger("next"))
                .expression(json.getString("expression"))
                .build());

        registerBlockType("condition", json -> new ConditionBlock.Builder()
                .id(json.getInteger("id"))
                .next(json.getInteger("next"))
                .expression(json.getString("expression"))
                .trueBranch(json.getInteger("trueBranch"))
                .falseBranch(json.getInteger("falseBranch", null))
                .build());

        registerBlockType("print", json -> new PrintBlock.Builder()
                .id(json.getInteger("id"))
                .next(json.getInteger("next"))
                .expression(json.getString("expression"))
                .build());

        registerBlockType("while", json -> new WhileBlock.Builder()
                .id(json.getInteger("id"))
                .next(json.getInteger("next"))
                .expression(json.getString("expression"))
                .body(json.getInteger("body"))
                .build());

        registerBlockType("end_condition", json -> new EndBlock.Builder()
                .id(json.getInteger("id"))
                .next(json.getInteger("next", null))
                .build());

        registerBlockType("end", json -> new EndBlock.Builder()
                .id(json.getInteger("id"))
                .build());
    }

    public void registerBlockType(String type, Function<JsonObject, AbstractBlock> builder) {
        blockBuilders.put(type, builder);
    }

    public AbstractBlock createBlock(JsonObject blockJson) {
        String type = blockJson.getString("type");
        Function<JsonObject, AbstractBlock> builder = blockBuilders.get(type);
        if (builder == null) {
            throw new IllegalArgumentException("Unknown block type: " + type);
        }
        return builder.apply(blockJson);
    }
}