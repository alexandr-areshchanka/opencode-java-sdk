package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.ExperimentalApi;
import opencode.sdk.api.WorkspaceApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.ExperimentalWorkspaceCreateRequest;
import opencode.sdk.model.GlobalSession;
import opencode.sdk.model.McpResource;
import opencode.sdk.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;
import java.util.Map;

public class ExperimentalExample {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentalExample.class);

    private final ExperimentalApi experimentalApi;
    private final WorkspaceApi workspaceApi;
    private final ResponseValidator validator;

    public ExperimentalExample(ApiClient apiClient) {
        this.experimentalApi = new ExperimentalApi(apiClient);
        this.workspaceApi = new WorkspaceApi(apiClient);
        this.validator = null;
    }

    public ExperimentalExample(ExampleContext context) {
        this.experimentalApi = new ExperimentalApi(context.getApiClient());
        this.workspaceApi = new WorkspaceApi(context.getApiClient());
        this.validator = context.getValidator();
    }

    public void demonstrateExperimentalApis() {
        try {
            logger.info("=== Experimental APIs Example ===");

            // List all sessions globally
            listGlobalSessions();

            // List workspaces
            listWorkspaces();

            // Create a workspace
            Workspace createdWorkspace = createWorkspace("example-workspace");

            // List workspaces again to show the new one
            listWorkspaces();

            // List MCP resources
            listMcpResources();

            logger.info("=== Experimental APIs Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during experimental operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during experimental operations: {}", e.getMessage(), e);
        }
    }

    private void listGlobalSessions() throws ApiException {
        logger.info("\n--- Listing Global Sessions ---");

        List<GlobalSession> sessions = experimentalApi.experimentalSessionList(
                null,  // directory
                null,  // workspace
                null,  // roots
                null,  // start
                null,  // cursor
                null,  // search
                new BigDecimal("10"),  // limit
                null   // archived
        );

        if (validator != null) {
            validator.validateCollection(sessions, "global sessions");
        }

        logger.info("Found {} global sessions:", sessions.size());
        for (GlobalSession session : sessions) {
            if (validator != null) {
                validator.validateNonNull(session.getId(), "session id");
            }

            logger.info("  - ID: {}, Title: {}",
                    session.getId(),
                    session.getTitle());
        }
    }

    private void listWorkspaces() throws ApiException {
        logger.info("\n--- Listing Workspaces ---");

        List<Workspace> workspaces = workspaceApi.experimentalWorkspaceList(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(workspaces, "workspaces");
        }

        logger.info("Found {} workspaces:", workspaces.size());
        for (Workspace workspace : workspaces) {
            if (validator != null) {
                validator.validateNonNull(workspace.getId(), "workspace id");
            }

            logger.info("  - ID: {}, Name: {}",
                    workspace.getId(),
                    workspace.getName());
        }
    }

    private Workspace createWorkspace(String name) throws ApiException {
        logger.info("\n--- Creating Workspace: {} ---", name);

        ExperimentalWorkspaceCreateRequest request = new ExperimentalWorkspaceCreateRequest();
        request.setType("git");
        request.setBranch("main");

        Workspace workspace = workspaceApi.experimentalWorkspaceCreate(
                null,  // directory
                null,  // workspace
                request
        );

        logger.info("Workspace created successfully:");
        logger.info("  ID: {}", workspace.getId());
        logger.info("  Name: {}", workspace.getName());
        logger.info("  Type: {}", workspace.getType());

        return workspace;
    }

    private void listMcpResources() throws ApiException {
        logger.info("\n--- Listing MCP Resources ---");

        Map<String, McpResource> resources = experimentalApi.experimentalResourceList(
                null,  // directory
                null   // workspace
        );

        logger.info("Found {} MCP resources:", resources.size());
        for (Map.Entry<String, McpResource> entry : resources.entrySet()) {
            McpResource resource = entry.getValue();
            logger.info("  - Key: {}, Name: {}, URI: {}",
                    entry.getKey(),
                    resource.getName(),
                    resource.getUri());
        }
    }

    public static void main(String[] args) {
        try {
            // Create client configuration
            ApiClient apiClient = new ApiClient();
            apiClient.updateBaseUri("http://localhost:4096");
            String credentials = "opencode:opencode123";
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
            apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));

            // Run the example
            ExperimentalExample example = new ExperimentalExample(apiClient);
            example.demonstrateExperimentalApis();

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
