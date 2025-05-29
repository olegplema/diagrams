package org.plema.controllers;

import io.vertx.core.CompositeFuture;
import io.vertx.core.Future;
import io.vertx.core.Vertx;
import io.vertx.core.json.JsonArray;
import io.vertx.core.json.JsonObject;
import io.vertx.ext.web.RoutingContext;
import org.plema.dtos.*;
import org.plema.services.GenerateCodeService;
import org.plema.services.RunDiagramService;
import org.plema.services.testing.TestDiagramService;
import org.plema.vertx.DiagramRouter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.atomic.AtomicLong;
import java.util.stream.Collectors;

public class DiagramController {

    private final GenerateCodeService generateCodeService = new GenerateCodeService();
    private final RunDiagramService runDiagramService;
    private final TestDiagramService testService;
    public DiagramController(Vertx vertx) {
        this.runDiagramService = new RunDiagramService(vertx);
        this.testService = new TestDiagramService(vertx);
    }

    public void generateCode(RoutingContext context) {
        try {
            Diagram diagram = context.get("convertedData");
            String code = generateCodeService.generateCode(diagram);
            context.response()
                    .setStatusCode(201)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new CodeResponse("Code generated", code)).encode());
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse(e.getMessage())).encode());
        } catch (Exception e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
        }
    }

    public void runDiagram(RoutingContext context) {
        try {
            Diagram diagram = context.get("convertedData");
            String clientSocketId = context.get("clientSocketId");
            runDiagramService.runDiagram(diagram, clientSocketId);
        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse(e.getMessage())).encode());
        } catch (Exception e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
        }
    }

    public void handleInteractiveTest(RoutingContext context) {
        try {
            Diagram diagram = context.get("diagram");
            TestCaseData testData = context.get("testCaseData");

            AtomicLong executionsCompleted = new AtomicLong(0);
            AtomicLong totalExecutions = new AtomicLong(0);

            testService.testInteractiveDiagram(diagram, testData.expectedOutput(), testData.input(),
                            partialResult -> {
                                executionsCompleted.incrementAndGet();
                                System.out.println("Progress: " + partialResult.maxExecutedSteps() + " steps completed");
                            })
                    .onSuccess(result -> {
                        JsonObject response = new JsonObject()
                                .put("successRates", new JsonObject(result.successRates().entrySet().stream()
                                        .collect(Collectors.toMap(
                                                e -> e.getKey().toString(),
                                                Map.Entry::getValue
                                        ))))
                                .put("maxExecutedSteps", result.maxExecutedSteps())
                                .put("totalExecutions", result.totalExecutions())
                                .put("completedExecutions", result.completedExecutions())
                                .put("interrupted", result.interrupted());

                        context.response()
                                .putHeader("content-type", "application/json")
                                .end(response.encode());
                    })
                    .onFailure(throwable -> {
                        context.response()
                                .setStatusCode(500)
                                .putHeader("Content-Type", "application/json")
                                .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
                    });

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse(e.getMessage())).encode());
        } catch (Exception e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
        }
    }

    public record TestCaseResult(NamedTestCase testCase, TestingResult result, Boolean passed, Long executionTime) {}
    public record TestSummary(Integer totalTests, Integer passedTests, Integer failedTests,
                              Long totalExecutionTime, Double averageSuccessRate) {}

    public void handleTestSuite(RoutingContext context) {
        try {
            Diagram diagram = context.get("diagram");
            TestCaseData testData = context.get("testCaseData");

            List<Future<TestCaseResult>> futures = new ArrayList<>();

            for (NamedTestCase testCase : testData.testSuite()) {
                long startTime = System.currentTimeMillis();

                Future<TestCaseResult> future = testService.testDiagram(diagram, testCase.expectedOutput(), testCase.input())
                        .map(result -> {
                            long executionTime = System.currentTimeMillis() - startTime;
                            boolean passed = result.successRates().getOrDefault(result.maxExecutedSteps(), 0.0) == 100.0;

                            return new TestCaseResult(testCase, result, passed, executionTime);
                        });

                futures.add(future);
            }

            CompositeFuture.all(futures.stream().map(f -> (Future)f).collect(Collectors.toList()))
                    .onSuccess(compositeFuture -> {
                        List<TestCaseResult> results = compositeFuture.list();

                        TestSummary summary = calculateSummary(results);

                        JsonObject response = new JsonObject()
                                .put("results", new JsonArray(results.stream()
                                        .map(DiagramController::testCaseResultToJson)
                                        .collect(Collectors.toList())))
                                .put("summary", testSummaryToJson(summary));

                        context.response()
                                .putHeader("content-type", "application/json")
                                .end(response.encode());
                    })
                    .onFailure(throwable -> {
                        context.response()
                                .setStatusCode(500)
                                .putHeader("Content-Type", "application/json")
                                .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
                    });

        } catch (IllegalArgumentException e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(400)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse(e.getMessage())).encode());
        } catch (Exception e) {
            e.printStackTrace();
            context.response()
                    .setStatusCode(500)
                    .putHeader("Content-Type", "application/json")
                    .end(JsonObject.mapFrom(new MessageResponse("Something went wrong")).encode());
        }
    }

    private static TestSummary calculateSummary(List<TestCaseResult> results) {
        int totalTests = results.size();
        int passedTests = results.stream().mapToInt(r -> r.passed() ? 1 : 0).sum();
        int failedTests = totalTests - passedTests;
        long totalExecutionTime = results.stream().mapToLong(TestCaseResult::executionTime).sum();
        double averageSuccessRate = results.stream()
                .mapToDouble(r -> r.result().successRates().getOrDefault(r.result().maxExecutedSteps(), 0.0))
                .average()
                .orElse(0.0);

        return new TestSummary(totalTests, passedTests, failedTests, totalExecutionTime, averageSuccessRate);
    }

    private static JsonObject testCaseResultToJson(TestCaseResult result) {
        return new JsonObject()
                .put("testCase", new JsonObject()
                        .put("name", result.testCase().name())
                        .put("input", new JsonArray(result.testCase().input()))
                        .put("expectedOutput", new JsonArray(result.testCase().expectedOutput())))
                .put("result", new JsonObject()
                        .put("successRates", new JsonObject(result.result().successRates().entrySet().stream()
                                .collect(Collectors.toMap(
                                        e -> e.getKey().toString(),
                                        Map.Entry::getValue
                                ))))
                        .put("maxExecutedSteps", result.result().maxExecutedSteps()))
                .put("passed", result.passed())
                .put("executionTime", result.executionTime());
    }

    private static JsonObject testSummaryToJson(TestSummary summary) {
        return new JsonObject()
                .put("totalTests", summary.totalTests())
                .put("passedTests", summary.passedTests())
                .put("failedTests", summary.failedTests())
                .put("totalExecutionTime", summary.totalExecutionTime())
                .put("averageSuccessRate", summary.averageSuccessRate());
    }
}
