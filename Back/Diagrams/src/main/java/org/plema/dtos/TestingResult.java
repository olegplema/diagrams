package org.plema.dtos;

import java.util.Map;

public record TestingResult(
        Map<Integer, Double> successRates,
        Integer maxExecutedSteps,
        Long totalExecutions,
        Long completedExecutions,
        Boolean interrupted
) {}