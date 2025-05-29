package org.plema.dtos;

import java.util.List;

public record TestCaseData(
        List<String> input,
        List<String> expectedOutput,
        List<NamedTestCase> testSuite,
        Integer maxSteps
) {}