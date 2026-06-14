package opencode.examples.plainjava.testing;

import opencode.sdk.api.PtyApi;
import opencode.sdk.api.SessionApi;
import opencode.sdk.invoker.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.lang.reflect.Constructor;
import java.util.List;

public class TestExecutor {

    private static final Logger logger = LoggerFactory.getLogger(TestExecutor.class);

    private final TestConfiguration config;
    private final ExampleRegistry registry;
    private final ResultReporter reporter;
    private final TestLogger testLogger;
    private CleanupManager cleanupManager;

    public TestExecutor(TestConfiguration config) {
        this.config = config;
        this.registry = new ExampleRegistry();
        this.testLogger = new TestLogger(config.getLogFile());
        this.reporter = new ResultReporter(config.isColorOutput(), testLogger, registry);
    }

    public TestResults executeAll() {
        TestResults results = new TestResults();
        long startTime = System.currentTimeMillis();

        List<String> exampleNames = config.isRunAll()
                ? registry.getAllExampleNames()
                : config.getExampleNames();

        logger.info("Executing {} examples", exampleNames.size());

        for (String exampleName : exampleNames) {
            if (!registry.hasExample(exampleName)) {
                logger.error("Example not found: {}", exampleName);
                TestResult result = new TestResult(exampleName);
                result.setSuccess(false);
                result.setErrorType("NotFound");
                result.setErrorMessage("Example class not found in registry");
                results.addResult(result);
                continue;
            }

            TestResult result = executeSingle(exampleName);
            results.addResult(result);
        }

        long endTime = System.currentTimeMillis();
        results.setTotalExecutionTimeMs(endTime - startTime);

        return results;
    }

    public TestResult executeSingle(String exampleName) {
        reporter.reportStart(exampleName);

        ExampleContext context = createContext();
        ExampleWrapper wrapper = new ExampleWrapper(context);

        TestResult result;
        try {
            Class<?> exampleClass = registry.getExample(exampleName);
            Object exampleInstance = createExampleInstance(exampleClass, context);
            result = wrapper.execute(exampleInstance, exampleName);

            if (result.isSuccess()) {
                reporter.reportSuccess(result);
            } else {
                reporter.reportFailure(result);
            }
        } catch (Exception e) {
            logger.error("Failed to execute example {}: {}", exampleName, e.getMessage(), e);
            result = new TestResult(exampleName);
            result.setSuccess(false);
            result.setErrorType("ExecutionError");
            result.setErrorMessage(e.getMessage());
            reporter.reportFailure(result);
        } finally {
            // Cleanup resources
            if (cleanupManager != null) {
                cleanupManager.cleanup(context.getResourceTracker());
            }
        }

        return result;
    }

    private ExampleContext createContext() {
        ApiClient apiClient = SdkClientFactory.createClient(
                config.getBaseUrl(), config.getUsername(), config.getPassword());

        // Initialize cleanup manager
        if (cleanupManager == null) {
            cleanupManager = new CleanupManager(new SessionApi(apiClient), new PtyApi(apiClient), testLogger);
        }

        ResourceTracker tracker = new ResourceTracker();
        ResponseValidator validator = new ResponseValidator();

        return new ExampleContext(apiClient, config, tracker, validator);
    }

    private Object createExampleInstance(Class<?> exampleClass, ExampleContext context) throws Exception {
        // Try to find constructor that accepts ExampleContext
        try {
            Constructor<?> contextConstructor = exampleClass.getConstructor(ExampleContext.class);
            return contextConstructor.newInstance(context);
        } catch (NoSuchMethodException e) {
            // Fall back to constructor that accepts ApiClient
            try {
                Constructor<?> apiConstructor = exampleClass.getConstructor(ApiClient.class);
                return apiConstructor.newInstance(context.getApiClient());
            } catch (NoSuchMethodException ex) {
                throw new RuntimeException("No suitable constructor found for example class: " + exampleClass.getName());
            }
        }
    }

    public ResultReporter getReporter() {
        return reporter;
    }

    public TestLogger getTestLogger() {
        return testLogger;
    }
}
