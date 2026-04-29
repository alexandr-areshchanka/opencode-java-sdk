package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.DefaultApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Arrays;
import java.util.Base64;
import java.util.Map;

public class McpExample {

    private static final Logger logger = LoggerFactory.getLogger(McpExample.class);

    private final DefaultApi api;
    private final ResponseValidator validator;

    public McpExample(DefaultApi api) {
        this.api = api;
        this.validator = null;
    }

    public McpExample(ExampleContext context) {
        this.api = context.getDefaultApi();
        this.validator = context.getValidator();
    }

    public void demonstrateMcpOperations() {
        try {
            logger.info("=== MCP Example ===");

            // Get current MCP status
            getMcpStatus();

            // Add an MCP server
            String serverName = "example-mcp-server";
            addMcpServer(serverName);

            // Connect to the MCP server
            connectMcpServer(serverName);

            // Get MCP resources
            listMcpResources();

            // Check MCP status again
            getMcpStatus();

            logger.info("=== MCP Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during MCP operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during MCP operations: {}", e.getMessage(), e);
        }
    }

    private void getMcpStatus() throws ApiException {
        logger.info("\n--- Getting MCP Status ---");

        Map<String, MCPStatus> statusMap = api.mcpStatus(
                null,  // directory
                null   // workspace
        );

        if (statusMap == null || statusMap.isEmpty()) {
            logger.info("No MCP servers configured");
        } else {
            logger.info("Found {} MCP server(s):", statusMap.size());
            for (Map.Entry<String, MCPStatus> entry : statusMap.entrySet()) {
                String serverName = entry.getKey();
                MCPStatus status = entry.getValue();
                logger.info("  - Server: {}", serverName);
                if (status.getActualInstance() != null) {
                    logger.info("    Status: {}", status.getActualInstance().getClass().getSimpleName());
                }
            }
        }
    }

    private void addMcpServer(String name) throws ApiException {
        logger.info("\n--- Adding MCP Server: {} ---", name);

        // Create a local MCP server configuration
        McpLocalConfig localConfig = new McpLocalConfig();
        localConfig.setType(McpLocalConfig.TypeEnum.LOCAL);
        localConfig.setCommand(Arrays.asList("node", "mcp-server.js"));
        localConfig.setEnabled(true);
        localConfig.setTimeout(5000);

        // Create the request config wrapper
        McpAddRequestConfig config = new McpAddRequestConfig(localConfig);

        // Create the add request
        McpAddRequest request = new McpAddRequest();
        request.setName(name);
        request.setConfig(config);

        Map<String, MCPStatus> result = api.mcpAdd(
                null,    // directory
                null,    // workspace
                request
        );

        if (result != null) {
            logger.info("MCP server added successfully");
            for (Map.Entry<String, MCPStatus> entry : result.entrySet()) {
                logger.info("  - {}: {}", entry.getKey(), entry.getValue().getActualInstance());
            }
        } else {
            logger.warn("MCP server add returned null");
        }
    }

    private void connectMcpServer(String name) throws ApiException {
        logger.info("\n--- Connecting to MCP Server: {} ---", name);

        Boolean result = api.mcpConnect(
                name,    // server name
                null,    // directory
                null     // workspace
        );

        if (result != null && result) {
            logger.info("MCP server '{}' connected successfully", name);
        } else {
            logger.warn("Failed to connect to MCP server '{}' or server not found", name);
        }
    }

    private void listMcpResources() throws ApiException {
        logger.info("\n--- Listing MCP Resources ---");

        Map<String, McpResource> resources = api.experimentalResourceList(
                null,  // directory
                null   // workspace
        );

        if (resources == null || resources.isEmpty()) {
            logger.info("No MCP resources available");
        } else {
            logger.info("Found {} MCP resource(s):", resources.size());
            for (Map.Entry<String, McpResource> entry : resources.entrySet()) {
                McpResource resource = entry.getValue();
                logger.info("  - Name: {}", resource.getName());
                logger.info("    URI: {}", resource.getUri());
                if (resource.getDescription() != null) {
                    logger.info("    Description: {}", resource.getDescription());
                }
                if (resource.getMimeType() != null) {
                    logger.info("    MIME Type: {}", resource.getMimeType());
                }
                logger.info("    Client: {}", resource.getClient());
            }
        }
    }

    private void startMcpAuth(String name) throws ApiException {
        logger.info("\n--- Starting MCP OAuth Flow: {} ---", name);

        try {
            McpAuthStart200Response response = api.mcpAuthStart(
                    name,    // server name
                    null,    // directory
                    null     // workspace
            );

            if (response != null) {
                logger.info("MCP OAuth flow started successfully");
                if (response.getAuthorizationUrl() != null) {
                    logger.info("  Authorization URL: {}", response.getAuthorizationUrl());
                }
            } else {
                logger.warn("MCP OAuth start returned null - server may not support OAuth");
            }
        } catch (ApiException e) {
            // OAuth may not be supported or configured for this server
            logger.info("  Note: MCP OAuth not available for server '{}' ({})", name, e.getMessage());
        }
    }

    public static void main(String[] args) {
        logger.info("Starting MCP Example");
        logger.info("===================");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));
        DefaultApi api = new DefaultApi(apiClient);

        try {
            // Run the example
            McpExample example = new McpExample(api);
            example.demonstrateMcpOperations();

            logger.info("\n");
            logger.info("===================");
            logger.info("Example completed successfully!");

        } catch (Exception e) {
            logger.error("Error running example: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
