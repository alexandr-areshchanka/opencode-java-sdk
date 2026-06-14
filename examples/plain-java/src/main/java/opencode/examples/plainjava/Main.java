package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResourceTracker;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.examples.plainjava.testing.SdkClientFactory;
import opencode.examples.plainjava.testing.TestConfiguration;
import opencode.sdk.api.GlobalApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting OpenCode Java SDK Examples");
        logger.info("====================================");

        // Build the shared client and context via the harness wiring
        TestConfiguration config = new TestConfiguration();
        ApiClient apiClient = SdkClientFactory.createClient(
                config.getBaseUrl(), config.getUsername(), config.getPassword());

        ResourceTracker tracker = new ResourceTracker();
        ResponseValidator validator = new ResponseValidator();
        ExampleContext context = new ExampleContext(apiClient, config, tracker, validator);

        // Launch EventStreamingExample in background thread (SSE is long-running)
        AtomicBoolean sseRunning = new AtomicBoolean(true);
        AtomicReference<String> sseResult = new AtomicReference<>("<no result>");
        Thread eventStreamingThread = new Thread(() -> {
            try {
                EventStreamingExample eventStreamingExample = new EventStreamingExample(context);
                eventStreamingExample.demonstrateEventStreaming();
                sseResult.set("completed");
            } catch (Exception e) {
                sseResult.set("error: " + e.getMessage());
                if (sseRunning.get()) {
                    logger.warn("EventStreamingExample error: {}", e.getMessage());
                }
            }
        }, "event-streaming-background");
        eventStreamingThread.setDaemon(true);
        eventStreamingThread.start();
        logger.info("EventStreamingExample started in background thread");

        try {
            // First, verify connection with health check
            performHealthCheck(new GlobalApi(apiClient));

            // Run System Info Example
            logger.info("\n");
            logger.info("========================================");
            SystemInfoExample systemInfoExample = new SystemInfoExample(context);
            systemInfoExample.demonstrateSystemInfo();

            // Run Configuration Example
            logger.info("\n");
            logger.info("========================================");
            ConfigurationExample configurationExample = new ConfigurationExample(context);
            configurationExample.demonstrateConfiguration();

            // Run Provider Example
            logger.info("\n");
            logger.info("========================================");
            ProviderExample providerExample = new ProviderExample(context);
            providerExample.demonstrateProviders();

            // Run Project Example
            logger.info("\n");
            logger.info("========================================");
            ProjectExample projectExample = new ProjectExample(context);
            projectExample.demonstrateProjectOperations();

            // Run File Operations Example
            logger.info("\n");
            logger.info("========================================");
            FileOperationsExample fileOperationsExample = new FileOperationsExample(context);
            fileOperationsExample.demonstrateFileOperations();

            // Run Session CRUD Example
            logger.info("\n");
            logger.info("========================================");
            SessionCrudExample sessionCrudExample = new SessionCrudExample(context);
            sessionCrudExample.demonstrateSessionCrud();

            // Run Session Advanced Example
            logger.info("\n");
            logger.info("========================================");
            SessionAdvancedExample sessionAdvancedExample = new SessionAdvancedExample(context);
            sessionAdvancedExample.demonstrateAdvancedSessionOperations();

            // Run Message Example
            logger.info("\n");
            logger.info("========================================");
            MessageExample messageExample = new MessageExample(context);
            messageExample.demonstrateMessaging();

            // ========== Phase 2 Examples ==========

            // Run DevTools Example
            logger.info("\n");
            logger.info("========================================");
            DevToolsExample devToolsExample = new DevToolsExample(context);
            devToolsExample.demonstrateDevTools();

            // Run Experimental Example
            logger.info("\n");
            logger.info("========================================");
            ExperimentalExample experimentalExample = new ExperimentalExample(context);
            experimentalExample.demonstrateExperimentalApis();

            // Run Instance Example
            logger.info("\n");
            logger.info("========================================");
            InstanceExample instanceExample = new InstanceExample(context);
            instanceExample.demonstrateInstanceManagement();

            // Run Interactive Example
            logger.info("\n");
            logger.info("========================================");
            InteractiveExample interactiveExample = new InteractiveExample(context);
            interactiveExample.demonstrateInteractiveApis();

            // Run MCP Example
            logger.info("\n");
            logger.info("========================================");
            McpExample mcpExample = new McpExample(context);
            mcpExample.demonstrateMcpOperations();

            // Run Todo Example
            logger.info("\n");
            logger.info("========================================");
            TodoExample todoExample = new TodoExample(context);
            todoExample.demonstrateTodoOperations();

            // Run VCS Example
            logger.info("\n");
            logger.info("========================================");
            VcsExample vcsExample = new VcsExample(context);
            vcsExample.demonstrateVcsOperations();

            // Run PTY Example
            logger.info("\n");
            logger.info("========================================");
            PtyExample ptyExample = new PtyExample(context);
            ptyExample.demonstratePtyOperations();

            logger.info("\n");
            logger.info("====================================");
            logger.info("All examples completed successfully!");

        } catch (Exception e) {
            logger.error("Error running examples: {}", e.getMessage(), e);
            System.exit(1);
        } finally {
            sseRunning.set(false);
            eventStreamingThread.interrupt();
            try {
                eventStreamingThread.join(2000);
            } catch (InterruptedException e) {
                Thread.currentThread().interrupt();
            }
            logger.info("EventStreamingExample background thread finished: {}", sseResult.get());
        }
    }

    private static void performHealthCheck(GlobalApi globalApi) {
        logger.info("\n--- Health Check ---");

        try {
            var health = globalApi.globalHealth();
            logger.info("Health check successful!");
            logger.info("  Healthy: {}", health.getHealthy());
            logger.info("  Version: {}", health.getVersion());
        } catch (ApiException e) {
            logger.error("Health check failed: {} - {}", e.getCode(), e.getMessage());
            throw new RuntimeException("Cannot connect to OpenCode server. Please ensure the server is running at http://localhost:4096", e);
        }
    }
}
