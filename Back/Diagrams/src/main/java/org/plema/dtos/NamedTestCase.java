package org.plema.dtos;

import java.util.List;

public record NamedTestCase(
        String name,
        List<String> input,
        List<String> expectedOutput
) {}