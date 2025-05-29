package org.plema.middlewares;

import io.vertx.core.Handler;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.plema.dtos.Diagram;
import org.plema.dtos.NamedTestCase;
import org.plema.dtos.TestCaseData;

import java.util.ArrayList;
import java.util.List;

public class TestCaseParsingMiddleware implements Handler<RoutingContext> {

    @Override
    public void handle(RoutingContext routingContext) {
        try {
            Diagram diagram = routingContext.get("convertedData");
            if (diagram == null) {
                routingContext.fail(400, new IllegalStateException("Diagram not found in context"));
                return;
            }

            JsonObject json = routingContext.body().asJsonObject();

            TestCaseData testCaseData = parseTestCases(json);

            routingContext.put("testCaseData", testCaseData);
            routingContext.put("diagram", diagram);

            routingContext.next();

        } catch (Exception e) {
            routingContext.fail(400, e);
        }
    }

    private TestCaseData parseTestCases(JsonObject json) {
        // Парсинг інтерактивного тесту
        if (json.containsKey("input") && json.containsKey("expectedOutput")) {
            List<String> input = parseInputArray(json.getJsonArray("input"));
            List<String> expectedOutput = parseOutputArray(json.getJsonArray("expectedOutput"));
            Integer maxSteps = json.getInteger("maxSteps");

            return new TestCaseData(
                    input,
                    expectedOutput,
                    null,
                    maxSteps
            );
        }

        // Парсинг одиночного виконання (без очікуваного результату)
        if (json.containsKey("input") && !json.containsKey("expectedOutput")) {
            List<String> input = parseInputArray(json.getJsonArray("input"));

            return new TestCaseData(
                    input,
                    null,
                    null,
                    null
            );
        }

        // Парсинг набору тестів
        if (json.containsKey("testCases")) {
            JsonArray testCasesJson = json.getJsonArray("testCases");
            List<NamedTestCase> testCases = new ArrayList<>();

            for (Object testCaseObj : testCasesJson) {
                JsonObject testCaseJson = (JsonObject) testCaseObj;
                String name = testCaseJson.getString("name");
                List<String> input = parseInputArray(testCaseJson.getJsonArray("input"));
                List<String> expectedOutput = parseOutputArray(testCaseJson.getJsonArray("expectedOutput"));

                testCases.add(new NamedTestCase(name, input, expectedOutput));
            }

            return new TestCaseData(
                    null,
                    null,
                    testCases,
                    null
            );
        }

        throw new IllegalArgumentException("Invalid test case format");
    }

    private List<String> parseStringArray(JsonArray jsonArray) {
        if (jsonArray == null) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        for (Object item : jsonArray) {
            result.add(item.toString());
        }
        return result;
    }

    private List<String> parseInputArray(JsonArray jsonArray) {
        if (jsonArray == null) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        for (Object item : jsonArray) {
            if (item instanceof Number) {
                result.add(item.toString());
            } else if (item instanceof String) {
                result.add((String) item);
            } else if (item instanceof Boolean) {
                result.add(item.toString());
            } else {
                result.add(String.valueOf(item));
            }
        }
        return result;
    }

    private List<String> parseOutputArray(JsonArray jsonArray) {
        if (jsonArray == null) {
            return new ArrayList<>();
        }

        List<String> result = new ArrayList<>();
        for (Object item : jsonArray) {
            if (item instanceof Number) {
                result.add(item.toString());
            } else if (item instanceof String) {
                result.add((String) item);
            } else if (item instanceof Boolean) {
                result.add(item.toString());
            } else {
                result.add(String.valueOf(item));
            }
        }
        return result;
    }
}