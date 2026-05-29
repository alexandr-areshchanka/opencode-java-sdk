package opencode.examples.plainjava;

import opencode.sdk.api.GlobalApi;
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

        try {
            // First, verify connection with health check
            performHealthCheck(new GlobalApi(apiClient));

            // Run System Info Example
            logger.info("\n");
            logger.info("========================================");
            SystemInfoExample systemInfoExample = new SystemInfoExample(apiClient);
            systemInfoExample.demonstrateSystemInfo();

            // Run Configuration Example
            logger.info("\n");
            logger.info("========================================");
            ConfigurationExample configurationExample = new ConfigurationExample(apiClient);
            configurationExample.demonstrateConfiguration();

            // Run Provider Example
            logger.info("\n");
            logger.info("========================================");
            ProviderExample providerExample = new ProviderExample(apiClient);
            providerExample.demonstrateProviders();

            // Run Project Example
            logger.info("\n");
            logger.info("========================================");
            ProjectExample projectExample = new ProjectExample(apiClient);
            projectExample.demonstrateProjectOperations();

            // Run File Operations Example
            logger.info("\n");
            logger.info("========================================");
            FileOperationsExample fileOperationsExample = new FileOperationsExample(apiClient);
            fileOperationsExample.demonstrateFileOperations();

            // Run Session CRUD Example
            logger.info("\n");
            logger.info("========================================");
            SessionCrudExample sessionCrudExample = new SessionCrudExample(apiClient);
            sessionCrudExample.demonstrateSessionCrud();

            // Run Session Advanced Example
            logger.info("\n");
            logger.info("========================================");
            SessionAdvancedExample sessionAdvancedExample = new SessionAdvancedExample(apiClient);
            sessionAdvancedExample.demonstrateAdvancedSessionOperations();

            // Run Message Example
            logger.info("\n");
            logger.info("========================================");
            MessageExample messageExample = new MessageExample(apiClient);
            messageExample.demonstrateMessaging();

            // ========== Phase 2 Examples ==========

            // Run DevTools Example
            logger.info("\n");
            logger.info("========================================");
            DevToolsExample devToolsExample = new DevToolsExample(apiClient);
            devToolsExample.demonstrateDevTools();

            // Run Experimental Example
            logger.info("\n");
            logger.info("========================================");
            ExperimentalExample experimentalExample = new ExperimentalExample(apiClient);
            experimentalExample.demonstrateExperimentalApis();

            // Run Instance Example
            logger.info("\n");
            logger.info("========================================");
            InstanceExample instanceExample = new InstanceExample(apiClient);
            instanceExample.demonstrateInstanceManagement();

            // Run Interactive Example
            logger.info("\n");
            logger.info("========================================");
            InteractiveExample interactiveExample = new InteractiveExample(apiClient);
            interactiveExample.demonstrateInteractiveApis();

            // Run MCP Example
            logger.info("\n");
            logger.info("========================================");
            McpExample mcpExample = new McpExample(apiClient);
            mcpExample.demonstrateMcpOperations();

            // Run Todo Example
            logger.info("\n");
            logger.info("========================================");
            TodoExample todoExample = new TodoExample(apiClient);
            todoExample.demonstrateTodoOperations();

            // Run VCS Example
            logger.info("\n");
            logger.info("========================================");
            VcsExample vcsExample = new VcsExample(apiClient);
            vcsExample.demonstrateVcsOperations();

            // Run Event Streaming Example
            logger.info("\n");
            logger.info("========================================");
            EventStreamingExample eventStreamingExample = new EventStreamingExample(apiClient);
            eventStreamingExample.demonstrateEventStreaming();

            // Run PTY Example
            logger.info("\n");
            logger.info("========================================");
            PtyExample ptyExample = new PtyExample(apiClient);
            ptyExample.demonstratePtyOperations();

            logger.info("\n");
            logger.info("====================================");
            logger.info("All examples completed successfully!");

        } catch (Exception e) {
            logger.error("Error running examples: {}", e.getMessage(), e);
            System.exit(1);
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
