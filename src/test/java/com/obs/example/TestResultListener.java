package com.obs.example;

import lombok.extern.slf4j.Slf4j;
import org.junit.jupiter.api.extension.*;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.ConcurrentMap;

@Slf4j
public class TestResultListener implements TestWatcher, BeforeAllCallback, AfterAllCallback, BeforeEachCallback {


    private final List<TestResult> testResults = new ArrayList<>();
    private final ConcurrentMap<String, Long> testStartTimes = new ConcurrentHashMap<>();

    private static class TestResult {
        String testName;
        boolean passed;
        String failureReason;
        long durationMillis;
        int assertionCount;

        TestResult(String testName, boolean passed, String failureReason, long durationMillis, int assertionCount) {
            this.testName = testName;
            this.passed = passed;
            this.failureReason = failureReason;
            this.durationMillis = durationMillis;
            this.assertionCount = assertionCount;
        }
    }

    @Override
    public void beforeAll(ExtensionContext context) {
        log.info("Starting test suite: {}", context.getDisplayName());
    }

    @Override
    public void beforeEach(ExtensionContext context) {
        testStartTimes.put(context.getUniqueId(), System.currentTimeMillis());
        log.info("Starting test: {}", context.getDisplayName());
    }

    @Override
    public void testSuccessful(ExtensionContext context) {
        long startTime = testStartTimes.remove(context.getUniqueId());
        long duration = System.currentTimeMillis() - startTime;
        // Note: JUnit doesn't expose assertion counts directly. We approximate by assuming MockMvc tests have multiple assertions.
        int assertionCount = estimateAssertionCount(context);
        testResults.add(new TestResult(context.getDisplayName(), true, null, duration, assertionCount));
        log.info("Test Success: {} (Duration: {}ms, Assertions: ~{})",
                context.getDisplayName(), duration, assertionCount);
    }

    @Override
    public void testFailed(ExtensionContext context, Throwable cause) {
        long startTime = testStartTimes.remove(context.getUniqueId());
        long duration = System.currentTimeMillis() - startTime;
        int assertionCount = estimateAssertionCount(context);
        testResults.add(new TestResult(context.getDisplayName(), false, cause.getMessage(), duration, assertionCount));
        log.error("Test Failed: {} (Reason: {}, Duration: {}ms, Assertions: ~{})",
                context.getDisplayName(), cause.getMessage(), duration, assertionCount);
    }

    @Override
    public void afterAll(ExtensionContext context) {
        long totalDuration = testResults.stream().mapToLong(tr -> tr.durationMillis).sum();
        long passedCount = testResults.stream().filter(tr -> tr.passed).count();
        long failedCount = testResults.stream().filter(tr -> !tr.passed).count();
        int totalAssertions = testResults.stream().mapToInt(tr -> tr.assertionCount).sum();

        log.info("\nTest Suite Summary: {}", context.getDisplayName());
        log.info("Total Tests: {}", testResults.size());
        log.info("Passed Tests: {} ({}%)", passedCount,
                testResults.isEmpty() ? 0 : (passedCount * 100 / testResults.size()));
        log.info("Failed Tests: {}", failedCount);
        log.info("Total Duration: {}ms", totalDuration);
        log.info("Total Assertions (approx.): {}", totalAssertions);

        if (!testResults.isEmpty()) {
            log.info("Detailed Results:");
            testResults.forEach(tr -> {
                String status = tr.passed ? "PASSED" : "FAILED";
                String failureInfo = tr.passed ? "" : " (Reason: " + tr.failureReason + ")";
                log.info("  - {}: {} (Duration: {}ms, Assertions: ~{}){}",
                        tr.testName, status, tr.durationMillis, tr.assertionCount, failureInfo);
            });
        }
    }

    private int estimateAssertionCount(ExtensionContext context) {
        // JUnit doesn't provide direct access to assertion counts.
        // For MockMvc tests, estimate based on typical assertion patterns (e.g., status, JSON paths).
        // For service/utils tests, assume 1-2 assertions unless complex.
        String testClass = context.getTestClass().map(Class::getSimpleName).orElse("");
        if (testClass.contains("ControllerTest")) {
            return 4; // Average for MockMvc tests (status + 3-5 JSON assertions)
        } else if (testClass.contains("ServiceTest") || testClass.contains("UtilsTest")) {
            return 2; // Average for service/utils tests (1-3 assertions)
        }
        return 1; // Default
    }
}