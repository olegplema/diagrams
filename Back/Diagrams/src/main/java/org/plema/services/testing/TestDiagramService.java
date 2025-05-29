package org.plema.services.testing;

import io.vertx.core.Future;
import io.vertx.core.Handler;
import io.vertx.core.Promise;
import io.vertx.core.Vertx;
import org.plema.Value;
import org.plema.dtos.TestingResult;
import org.plema.dtos.Diagram;
import org.plema.models.Variable;
import org.plema.services.AbstractDiagramService;

import java.util.*;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

record ExecutionResult(List<Integer> executedThreads, List<String> output) {}

record ExecutionState(List<Integer> executedThreads, List<Integer> workingThreads) {}

record ThreadsState(List<SteppableBlocksCodeRunner> visitors, MockOutput stdout) {}

public class TestDiagramService extends AbstractDiagramService {

    private final Vertx vertx;

    public TestDiagramService(Vertx vertx) {
        this.vertx = vertx;
    }

    public Future<TestingResult> testDiagram(Diagram diagram, List<String> expectedOutput, List<String> stdin) {
        Promise<TestingResult> promise = Promise.promise();

        vertx.executeBlocking(blockingPromise -> {
            try {
                TestingResult result = performTesting(diagram, expectedOutput, stdin);
                blockingPromise.complete(result);
            } catch (Exception e) {
                blockingPromise.fail(e);
            }
        }, asyncResult -> {
            if (asyncResult.succeeded()) {
                promise.complete((TestingResult) asyncResult.result());
            } else {
                promise.fail(asyncResult.cause());
            }
        });

        return promise.future();
    }

    public Future<TestingResult> testInteractiveDiagram(Diagram diagram, List<String> expectedOutput,
                                                        List<String> stdin, Handler<TestingResult> progressHandler) {
        Promise<TestingResult> promise = Promise.promise();

        vertx.executeBlocking(blockingPromise -> {
            try {
                TestingResult result = performInteractiveTesting(diagram, expectedOutput, stdin, progressHandler);
                blockingPromise.complete(result);
            } catch (Exception e) {
                blockingPromise.fail(e);
            }
        }, asyncResult -> {
            if (asyncResult.succeeded()) {
                promise.complete((TestingResult) asyncResult.result());
            } else {
                promise.fail(asyncResult.cause());
            }
        });

        return promise.future();
    }

    private TestingResult performTesting(Diagram diagram, List<String> expectedOutput, List<String> stdin) {
        List<ExecutionResult> results = new ArrayList<>();
        Queue<ExecutionState> queue = new LinkedList<>();

        List<Integer> initialWorkingThreads = IntStream.range(0, diagram.threads().size())
                .boxed().collect(Collectors.toList());
        queue.offer(new ExecutionState(new ArrayList<>(), initialWorkingThreads));

        while (!queue.isEmpty()) {
            ExecutionState currentState = queue.poll();
            List<Integer> executedThreads = currentState.executedThreads();
            List<Integer> workingThreads = currentState.workingThreads();

            for (Integer threadIndex : workingThreads) {
                ThreadsState threadsState = replayOperations(diagram, executedThreads, stdin);

                if (threadIndex < threadsState.visitors().size()) {
                    SteppableBlocksCodeRunner runner = threadsState.visitors().get(threadIndex);
                    runner.step();

                    List<Integer> nextExecutedThreads = new ArrayList<>(executedThreads);
                    nextExecutedThreads.add(threadIndex);

                    List<Integer> nextWorkingThreads = workingThreads.stream()
                            .filter(t -> !t.equals(threadIndex) || !runner.isDone())
                            .collect(Collectors.toList());

                    // ВИПРАВЛЕННЯ: Додаємо результат тільки коли всі потоки завершені
                    if (nextWorkingThreads.isEmpty()) {
                        results.add(new ExecutionResult(nextExecutedThreads, threadsState.stdout().getLines()));
                    }

                    if (!nextWorkingThreads.isEmpty()) {
                        queue.offer(new ExecutionState(nextExecutedThreads, nextWorkingThreads));
                    }
                }
            }
        }

        return analyzeExecutionResults(results, expectedOutput);
    }

    private TestingResult performInteractiveTesting(Diagram diagram, List<String> expectedOutput,
                                                    List<String> stdin, Handler<TestingResult> progressHandler) {
        List<ExecutionResult> results = new ArrayList<>();
        Queue<ExecutionState> queue = new LinkedList<>();
        AtomicBoolean interrupted = new AtomicBoolean(false);

        List<Integer> initialWorkingThreads = IntStream.range(0, diagram.threads().size())
                .boxed().collect(Collectors.toList());
        queue.offer(new ExecutionState(new ArrayList<>(), initialWorkingThreads));

        int processedStates = 0;

        while (!queue.isEmpty() && !interrupted.get()) {
            ExecutionState currentState = queue.poll();
            List<Integer> executedThreads = currentState.executedThreads();
            List<Integer> workingThreads = currentState.workingThreads();

            for (Integer threadIndex : workingThreads) {
                if (interrupted.get()) break;

                ThreadsState threadsState = replayOperations(diagram, executedThreads, stdin);

                if (threadIndex < threadsState.visitors().size()) {
                    SteppableBlocksCodeRunner runner = threadsState.visitors().get(threadIndex);
                    runner.step();

                    List<Integer> nextExecutedThreads = new ArrayList<>(executedThreads);
                    nextExecutedThreads.add(threadIndex);

                    List<Integer> nextWorkingThreads = workingThreads.stream()
                            .filter(t -> !t.equals(threadIndex) || !runner.isDone())
                            .collect(Collectors.toList());

                    // ВИПРАВЛЕННЯ: Додаємо результат тільки коли всі потоки завершені
                    if (nextWorkingThreads.isEmpty()) {
                        results.add(new ExecutionResult(nextExecutedThreads, threadsState.stdout().getLines()));
                    }

                    if (!nextWorkingThreads.isEmpty()) {
                        queue.offer(new ExecutionState(nextExecutedThreads, nextWorkingThreads));
                    }
                }
            }

            processedStates++;
            if (processedStates % 100 == 0 && progressHandler != null) {
                // Створюємо проміжний результат
                TestingResult partialResult = createPartialResult(results, expectedOutput, interrupted.get());
                progressHandler.handle(partialResult);
            }
        }

        return createFinalResult(results, expectedOutput, interrupted.get());
    }

    private TestingResult createPartialResult(List<ExecutionResult> completeResults, List<String> expected, boolean isInterrupted) {
        return analyzeCompleteResults(completeResults, expected);
    }

    private TestingResult analyzeCompleteResults(List<ExecutionResult> completeResults, List<String> expected) {
        if (completeResults.isEmpty()) {
            return new TestingResult(new HashMap<>(), 0, 0L, 0L, false);
        }

        // Рахуємо тільки кількість кроків для ЗАВЕРШЕНИХ програм
        Map<Integer, Integer> stepCounts = new HashMap<>();
        Map<Integer, Integer> successCounts = new HashMap<>();
        int maxSteps = 0;

        for (ExecutionResult result : completeResults) {
            int steps = result.executedThreads().size();
            maxSteps = Math.max(maxSteps, steps);

            stepCounts.put(steps, stepCounts.getOrDefault(steps, 0) + 1);

            if (result.output().equals(expected)) {
                successCounts.put(steps, successCounts.getOrDefault(steps, 0) + 1);
            }
        }

        // Накопичувальна статистика
        Map<Integer, Double> successRates = new HashMap<>();

        for (int k = 1; k <= maxSteps; k++) {
            int totalUpToK = 0;
            int successUpToK = 0;

            // Рахуємо програми, що завершилися за ≤k кроків
            for (int steps = 1; steps <= k; steps++) {
                totalUpToK += stepCounts.getOrDefault(steps, 0);
                successUpToK += successCounts.getOrDefault(steps, 0);
            }

            if (totalUpToK > 0) {
                successRates.put(k, (successUpToK * 100.0) / totalUpToK);
            } else {
                successRates.put(k, 0.0);
            }
        }

        long totalExecutions = completeResults.size();
        return new TestingResult(successRates, maxSteps, totalExecutions, totalExecutions, false);
    }
    private TestingResult createFinalResult(List<ExecutionResult> completeResults, List<String> expected, boolean isInterrupted) {
        TestingResult result = analyzeCompleteResults(completeResults, expected);
        // Оновлюємо статус переривання
        return new TestingResult(
                result.successRates(),
                result.maxExecutedSteps(),
                result.totalExecutions(),
                result.completedExecutions(),
                isInterrupted
        );
    }

    private ThreadsState replayOperations(Diagram diagram, List<Integer> executedThreads, List<String> stdin) {
        Map<String, Value> variables = initializeVariables(diagram);
        List<SteppableBlocksCodeRunner> visitors = new ArrayList<>();
        MockOutput mockOutput = new MockOutput();
        MockInput mockStdin = new MockInput(stdin);
        MockWebSocketHandler mockHandler = new MockWebSocketHandler(mockOutput, mockStdin);

        for (int i = 0; i < diagram.threads().size(); i++) {
            SteppableBlocksCodeRunner runner = new SteppableBlocksCodeRunner(variables, mockHandler, "test-client-" + i);
            runner.setCurrentBlock(diagram.threads().get(i));
            visitors.add(runner);
        }

        for (Integer threadIndex : executedThreads) {
            if (threadIndex < visitors.size()) {
                SteppableBlocksCodeRunner runner = visitors.get(threadIndex);
                runner.step();
            }
        }

        return new ThreadsState(visitors, mockOutput);
    }

    private Map<String, Value> initializeVariables(Diagram diagram) {
        Map<String, Value> variables = new HashMap<>();
        for (Variable variable : diagram.variables()) {
            variables.put(variable.getName(),
                    new Value(variable.getType().getDefaultValue(), variable.getType()));
        }
        return variables;
    }

    private TestingResult analyzeExecutionResults(List<ExecutionResult> executionResults, List<String> expected) {
        if (executionResults.isEmpty()) {
            return new TestingResult(new HashMap<>(), 0, 0L, 0L, false);
        }

        Map<Integer, Integer> exactStepCounts = new HashMap<>(); // Точна кількість за конкретні кроки
        Map<Integer, Integer> exactSuccessCounts = new HashMap<>(); // Успішні за конкретні кроки
        int maxExecutedSteps = 0;

        // Підрахунок результатів за точну кількість кроків
        for (ExecutionResult executionResult : executionResults) {
            List<Integer> executedThreads = executionResult.executedThreads();
            List<String> output = executionResult.output();

            int executedSteps = executedThreads.size();
            maxExecutedSteps = Math.max(maxExecutedSteps, executedSteps);

            exactStepCounts.put(executedSteps, exactStepCounts.getOrDefault(executedSteps, 0) + 1);

            if (output.equals(expected)) {
                exactSuccessCounts.put(executedSteps, exactSuccessCounts.getOrDefault(executedSteps, 0) + 1);
            }
        }

        // Накопичувальна статистика (≤ i кроків)
        Map<Integer, Integer> cumulativeTotalCounts = new HashMap<>();
        Map<Integer, Integer> cumulativeSuccessCounts = new HashMap<>();

        for (int i = 1; i <= maxExecutedSteps; i++) {
            int cumulativeTotal = 0;
            int cumulativeSuccess = 0;

            // Сумуємо всі виконання з кількістю кроків ≤ i
            for (int j = 1; j <= i; j++) {
                cumulativeTotal += exactStepCounts.getOrDefault(j, 0);
                cumulativeSuccess += exactSuccessCounts.getOrDefault(j, 0);
            }

            cumulativeTotalCounts.put(i, cumulativeTotal);
            cumulativeSuccessCounts.put(i, cumulativeSuccess);
        }

        // Обчислення відсотків успішності
        Map<Integer, Double> successRates = new HashMap<>();
        for (int i = 1; i <= maxExecutedSteps; i++) {
            int total = cumulativeTotalCounts.getOrDefault(i, 0);
            int success = cumulativeSuccessCounts.getOrDefault(i, 0);

            if (total > 0) {
                successRates.put(i, (success * 100.0) / total);
            } else {
                successRates.put(i, 0.0);
            }
        }

        long totalExecutions = (long) executionResults.size();
        long completedExecutions = totalExecutions;
        boolean interrupted = false;

        return new TestingResult(successRates, maxExecutedSteps, totalExecutions, completedExecutions, interrupted);
    }
}