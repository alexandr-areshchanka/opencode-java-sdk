package opencode.examples.plainjava.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Set;

public class ResultReporter {

    private static final Logger logger = LoggerFactory.getLogger(ResultReporter.class);

    private final boolean colorOutput;
    private final TestLogger testLogger;
    private final ExampleRegistry registry;

    // ANSI color codes
    private static final String ANSI_RESET = "\u001B[0m";
    private static final String ANSI_GREEN = "\u001B[32m";
    private static final String ANSI_RED = "\u001B[31m";
    private static final String ANSI_YELLOW = "\u001B[33m";
    private static final String ANSI_BLUE = "\u001B[34m";

    public ResultReporter(boolean colorOutput, TestLogger testLogger, ExampleRegistry registry) {
        this.colorOutput = colorOutput;
        this.testLogger = testLogger;
        this.registry = registry;
    }

    public void reportStart(String exampleName) {
        String message = "Running example: " + exampleName;
        logger.info(colorize(message, ANSI_BLUE));
        testLogger.logInfo(message);
    }

    public void reportStartWithConfig(String exampleName, String provider, String model) {
        String message = String.format("Running example: %s (provider=%s, model=%s)", exampleName, provider, model);
        logger.info(colorize(message, ANSI_BLUE));
        testLogger.logInfo(message);
    }

    public void reportSuccess(TestResult result) {
        String message = String.format("✓ %s completed successfully in %s",
                result.getExampleName(),
                formatDuration(result.getExecutionTimeMs()));
        logger.info(colorize(message, ANSI_GREEN));
        testLogger.logInfo(message);
    }

    public void reportFailure(TestResult result) {
        String message = String.format("✗ %s failed (%s) in %s: %s",
                result.getExampleName(),
                result.getErrorType(),
                formatDuration(result.getExecutionTimeMs()),
                result.getErrorMessage());
        logger.error(colorize(message, ANSI_RED));
        testLogger.logInfo(message);
    }

    public void reportSummary(TestResults results) {
        logger.info("");
        logger.info("========================================");
        logger.info("Test Execution Summary");
        logger.info("========================================");

        String totalMessage = String.format("Total Examples: %d", results.getTotalCount());
        String passedMessage = String.format("Passed: %d", results.getPassedCount());
        String failedMessage = String.format("Failed: %d", results.getFailedCount());
        String successRateMessage = String.format("Success Rate: %.2f%%", results.getSuccessRate());
        String durationMessage = String.format("Total Duration: %s", formatDuration(results.getTotalExecutionTimeMs()));

        logger.info(totalMessage);
        logger.info(colorize(passedMessage, ANSI_GREEN));

        if (results.getFailedCount() > 0) {
            logger.info(colorize(failedMessage, ANSI_RED));
        } else {
            logger.info(failedMessage);
        }

        logger.info(successRateMessage);
        logger.info(durationMessage);

        testLogger.logInfo(totalMessage);
        testLogger.logInfo(passedMessage);
        testLogger.logInfo(failedMessage);
        testLogger.logInfo(successRateMessage);
        testLogger.logInfo(durationMessage);

        if (results.getFailedCount() > 0) {
            logger.info("");
            logger.info("Failed Examples:");
            for (TestResult result : results.getResults()) {
                if (!result.isSuccess()) {
                    logger.info(colorize(String.format("  - %s: %s", result.getExampleName(), result.getErrorMessage()), ANSI_RED));
                }
            }
        }

        // Report demonstrated endpoints
        reportDemonstratedEndpoints(results);

        logger.info("========================================");
    }

    private void reportDemonstratedEndpoints(TestResults results) {
        logger.info("");
        logger.info("Demonstrated Endpoints:");

        Set<String> allEndpoints = registry.getAllDemonstratedEndpoints();
        int testedEndpoints = 0;

        for (TestResult result : results.getResults()) {
            if (result.isSuccess()) {
                Set<String> endpoints = registry.getDemonstratedEndpoints(result.getExampleName());
                testedEndpoints += endpoints.size();
            }
        }

        logger.info("  Total Demonstrated Endpoints: {}", allEndpoints.size());
        logger.info("  Successfully Demonstrated Endpoints: {}", testedEndpoints);

        // Show demonstrated endpoints by example
        logger.info("");
        logger.info("Endpoints by Example:");
        for (TestResult result : results.getResults()) {
            Set<String> endpoints = registry.getDemonstratedEndpoints(result.getExampleName());
            String status = result.isSuccess() ? colorize("✓", ANSI_GREEN) : colorize("✗", ANSI_RED);
            logger.info("  {} {} - {} endpoint(s)", status, result.getExampleName(), endpoints.size());
        }
    }

    private String formatDuration(long milliseconds) {
        if (milliseconds < 1000) {
            return milliseconds + "ms";
        } else if (milliseconds < 60000) {
            return String.format("%.2fs", milliseconds / 1000.0);
        } else {
            long minutes = milliseconds / 60000;
            long seconds = (milliseconds % 60000) / 1000;
            return String.format("%dm %ds", minutes, seconds);
        }
    }

    private String colorize(String text, String colorCode) {
        if (!colorOutput) {
            return text;
        }
        return colorCode + text + ANSI_RESET;
    }
}
