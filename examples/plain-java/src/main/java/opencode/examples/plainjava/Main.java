package opencode.examples.plainjava;

import opencode.sdk.api.DefaultApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class Main {

    private static final Logger logger = LoggerFactory.getLogger(Main.class);

    public static void main(String[] args) {
        logger.info("Starting OpenCode Java SDK Examples");
        logger.info("====================================");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));
        DefaultApi api = new DefaultApi(apiClient);

        try {
            // First, verify connection with health check
            performHealthCheck(api);

            // Run System Info Example
            logger.info("\n");
            logger.info("========================================");
            SystemInfoExample systemInfoExample = new SystemInfoExample(api);
            systemInfoExample.demonstrateSystemInfo();

            // Run Configuration Example
            logger.info("\n");
            logger.info("========================================");
            ConfigurationExample configurationExample = new ConfigurationExample(api);
            configurationExample.demonstrateConfiguration();

            // Run Provider Example
            logger.info("\n");
            logger.info("========================================");
            ProviderExample providerExample = new ProviderExample(api);
            providerExample.demonstrateProviders();

            // Run Project Example
            logger.info("\n");
            logger.info("========================================");
            ProjectExample projectExample = new ProjectExample(api);
            projectExample.demonstrateProjectOperations();

            // Run File Operations Example
            logger.info("\n");
            logger.info("========================================");
            FileOperationsExample fileOperationsExample = new FileOperationsExample(api);
            fileOperationsExample.demonstrateFileOperations();

            // Run Session CRUD Example
            logger.info("\n");
            logger.info("========================================");
            SessionCrudExample sessionCrudExample = new SessionCrudExample(api, apiClient);
            sessionCrudExample.demonstrateSessionCrud();

            // Run Session Advanced Example
            logger.info("\n");
            logger.info("========================================");
            SessionAdvancedExample sessionAdvancedExample = new SessionAdvancedExample(api, apiClient);
            sessionAdvancedExample.demonstrateAdvancedSessionOperations();

            // Run Message Example
            logger.info("\n");
            logger.info("========================================");
            MessageExample messageExample = new MessageExample(api);
            messageExample.demonstrateMessaging();

            // ========== Phase 2 Examples ==========

            // Run DevTools Example
            logger.info("\n");
            logger.info("========================================");
            DevToolsExample devToolsExample = new DevToolsExample(api);
            devToolsExample.demonstrateDevTools();

            // Run Experimental Example
            logger.info("\n");
            logger.info("========================================");
            ExperimentalExample experimentalExample = new ExperimentalExample(api);
            experimentalExample.demonstrateExperimentalApis();

            // Run Instance Example
            logger.info("\n");
            logger.info("========================================");
            InstanceExample instanceExample = new InstanceExample(api);
            instanceExample.demonstrateInstanceManagement();

            // Run Interactive Example
            logger.info("\n");
            logger.info("========================================");
            InteractiveExample interactiveExample = new InteractiveExample(api);
            interactiveExample.demonstrateInteractiveApis();

            // Run MCP Example
            logger.info("\n");
            logger.info("========================================");
            McpExample mcpExample = new McpExample(api);
            mcpExample.demonstrateMcpOperations();

            // Run Todo Example
            logger.info("\n");
            logger.info("========================================");
            TodoExample todoExample = new TodoExample(api);
            todoExample.demonstrateTodoOperations();

            // Run VCS Example
            logger.info("\n");
            logger.info("========================================");
            VcsExample vcsExample = new VcsExample(api);
            vcsExample.demonstrateVcsOperations();

            // Run Event Streaming Example
            logger.info("\n");
            logger.info("========================================");
            EventStreamingExample eventStreamingExample = new EventStreamingExample(api);
            eventStreamingExample.demonstrateEventStreaming();

            // Run PTY Example
            logger.info("\n");
            logger.info("========================================");
            PtyExample ptyExample = new PtyExample(api);
            ptyExample.demonstratePtyOperations();

            logger.info("\n");
            logger.info("====================================");
            logger.info("All examples completed successfully!");

        } catch (Exception e) {
            logger.error("Error running examples: {}", e.getMessage(), e);
            System.exit(1);
        }
    }

    private static void performHealthCheck(DefaultApi api) {
        logger.info("\n--- Health Check ---");

        try {
            var health = api.globalHealth();
            logger.info("Health check successful!");
            logger.info("  Healthy: {}", health.getHealthy());
            logger.info("  Version: {}", health.getVersion());
        } catch (ApiException e) {
            logger.error("Health check failed: {} - {}", e.getCode(), e.getMessage());
            throw new RuntimeException("Cannot connect to OpenCode server. Please ensure the server is running at http://localhost:4096", e);
        }
    }
}
