package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.DefaultApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Project;
import opencode.sdk.model.ProjectUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;

public class ProjectExample {

    private static final Logger logger = LoggerFactory.getLogger(ProjectExample.class);

    private final DefaultApi api;
    private final ResponseValidator validator;

    public ProjectExample(DefaultApi api) {
        this.api = api;
        this.validator = null;
    }

    public ProjectExample(ExampleContext context) {
        this.api = context.getDefaultApi();
        this.validator = context.getValidator();
    }

    public void demonstrateProjectOperations() {
        try {
            logger.info("=== Project Example ===");

            // List all projects
            listProjects();

            // Get current project
            Project currentProject = getCurrentProject();

            // Update project if current project exists
            if (currentProject != null) {
                updateProject(currentProject.getId());
            }

            logger.info("=== Project Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during project operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during project operations: {}", e.getMessage(), e);
        }
    }

    private void listProjects() throws ApiException {
        logger.info("\n--- Listing All Projects ---");

        List<Project> projects = api.projectList(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(projects, "projects");
        }

        logger.info("Found {} project(s):", projects.size());
        for (Project project : projects) {
            if (validator != null) {
                validator.validateNonNull(project.getId(), "project id");
                validator.validateNonNull(project.getWorktree(), "project worktree");
            }

            logger.info("  Project ID: {}", project.getId());
            logger.info("    Worktree: {}", project.getWorktree());
            if (project.getName() != null) {
                logger.info("    Name: {}", project.getName());
            }
            if (project.getVcs() != null) {
                logger.info("    VCS: {}", project.getVcs());
            }
            if (project.getTime() != null && project.getTime().getCreated() != null) {
                logger.info("    Created: {}", project.getTime().getCreated());
            }
            if (!project.getSandboxes().isEmpty()) {
                logger.info("    Sandboxes: {}", project.getSandboxes());
            }
        }
    }

    private Project getCurrentProject() throws ApiException {
        logger.info("\n--- Retrieving Current Project ---");

        Project project = api.projectCurrent(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateNonNull(project, "current project");
            validator.validateNonNull(project.getId(), "project id");
        }

        logger.info("Current project retrieved successfully!");
        logger.info("  Project ID: {}", project.getId());
        logger.info("  Worktree: {}", project.getWorktree());
        if (project.getName() != null) {
            logger.info("  Name: {}", project.getName());
        }
        if (project.getVcs() != null) {
            logger.info("  VCS: {}", project.getVcs());
        }
        if (project.getTime() != null && project.getTime().getCreated() != null) {
            logger.info("  Created: {}", project.getTime().getCreated());
        }
        if (!project.getSandboxes().isEmpty()) {
            logger.info("  Sandboxes: {}", project.getSandboxes());
        }

        return project;
    }

    private void updateProject(String projectId) throws ApiException {
        logger.info("\n--- Updating Project ---");

        // Create update request with only the fields we want to modify
        ProjectUpdateRequest updateRequest = new ProjectUpdateRequest();
        updateRequest.setName("Updated Project Name");

        Project updatedProject = api.projectUpdate(
                projectId,      // projectID (required)
                null,           // directory
                null,           // workspace
                updateRequest   // update request
        );

        if (validator != null) {
            validator.validateNonNull(updatedProject, "updated project");
            validator.validateNonNull(updatedProject.getId(), "updated project id");
        }

        logger.info("Project updated successfully!");
        logger.info("  Project ID: {}", updatedProject.getId());
        if (updatedProject.getName() != null) {
            logger.info("  New Name: {}", updatedProject.getName());
        }
    }

    public static void main(String[] args) {
        logger.info("Starting Project Example");
        logger.info("========================");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));
        DefaultApi api = new DefaultApi(apiClient);

        try {
            // Run the example
            ProjectExample example = new ProjectExample(api);
            example.demonstrateProjectOperations();

            logger.info("\n");
            logger.info("========================");
            logger.info("Example completed successfully!");

        } catch (Exception e) {
            logger.error("Error running example: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
