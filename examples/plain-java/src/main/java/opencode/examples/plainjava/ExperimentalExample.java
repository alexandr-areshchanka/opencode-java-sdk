package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.ExperimentalApi;
import opencode.sdk.api.WorkspaceApi;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.ExperimentalWorkspaceCreateRequest;
import opencode.sdk.model.GlobalSession;
import opencode.sdk.model.McpResource;
import opencode.sdk.model.Workspace;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.List;
import java.util.Map;

public class ExperimentalExample {

    private static final Logger logger = LoggerFactory.getLogger(ExperimentalExample.class);

    private final ExperimentalApi experimentalApi;
    private final WorkspaceApi workspaceApi;
    private final ResponseValidator validator;

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

            // Create a workspace (may fail if server lacks the workspace adapter plugin)
            createWorkspace("example-workspace");

            // List workspaces again
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

    private static final String DEFAULT_WORKSPACE_ADAPTER = "git";
    private static final String DEFAULT_BRANCH = "main";

    private Workspace createWorkspace(String name) {
        logger.info("\n--- Creating Workspace: {} ---", name);

        ExperimentalWorkspaceCreateRequest request = new ExperimentalWorkspaceCreateRequest();
        request.setType(DEFAULT_WORKSPACE_ADAPTER);
        request.setBranch(DEFAULT_BRANCH);

        try {
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
        } catch (ApiException e) {
            // Workspace adapters are plugin-registered; the server may not support the requested adapter type
            logger.warn("Workspace creation skipped: {}", e.getMessage());
            return null;
        }
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

}
