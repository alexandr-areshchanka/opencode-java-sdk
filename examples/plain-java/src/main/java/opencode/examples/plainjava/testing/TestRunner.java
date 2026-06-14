package opencode.examples.plainjava.testing;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class TestRunner {

    private static final Logger logger = LoggerFactory.getLogger(TestRunner.class);

    public static void main(String[] args) {
        TestExecutor executor = null;
        int exitCode = 0;
        try {
            // Parse command-line arguments
            ArgumentParser parser = new ArgumentParser();
            TestConfiguration config = parser.parse(args);

            if (config == null) {
                // Invalid arguments, usage already printed
                exitCode = 1;
                return;
            }

            // Load environment variables as defaults
            EnvironmentLoader loader = new EnvironmentLoader();
            loader.loadIntoConfiguration(config);

            // Mask sensitive data in logs
            logger.info("Starting test execution with configuration: {}",
                    SensitiveDataMasker.maskAllSensitiveData(config.toString(), config));

            // Create executor
            executor = new TestExecutor(config);

            // Execute tests
            TestResults results = executor.executeAll();

            // Report results
            executor.getReporter().reportSummary(results);

            // Flush logs
            executor.getTestLogger().flush();

            // Exit with appropriate status code
            exitCode = results.getFailedCount() > 0 ? 1 : 0;

        } catch (Exception e) {
            logger.error("Fatal error during test execution: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            exitCode = 1;
        } finally {
            // Always close the test logger (flushes the underlying PrintWriter)
            // before terminating the JVM, on both success and error paths.
            if (executor != null) {
                executor.getTestLogger().close();
            }
            System.exit(exitCode);
        }
    }
}
