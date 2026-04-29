package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.DefaultApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.VcsInfo;
import opencode.sdk.model.Worktree;
import opencode.sdk.model.WorktreeCreateInput;
import opencode.sdk.model.WorktreeRemoveInput;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;

public class VcsExample {

    private static final Logger logger = LoggerFactory.getLogger(VcsExample.class);

    private final DefaultApi api;
    private final ResponseValidator validator;

    public VcsExample(DefaultApi api) {
        this.api = api;
        this.validator = null;
    }

    public VcsExample(ExampleContext context) {
        this.api = context.getDefaultApi();
        this.validator = context.getValidator();
    }

    public void demonstrateVcsOperations() {
        try {
            logger.info("=== VCS and Worktree Example ===");

            // Get VCS branch info
            getVcsInfo();

            // List worktrees
            listWorktrees();

            // Create a worktree (demonstration - may fail if not in a git repo)
            Worktree createdWorktree = createWorktree("example-worktree");
            if (createdWorktree != null) {
                logger.info("Created worktree: {}", createdWorktree.getName());

                // Remove the worktree we just created
                removeWorktree(createdWorktree.getDirectory());
            }

            logger.info("=== VCS and Worktree Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during VCS operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during VCS operations: {}", e.getMessage(), e);
        }
    }

    private void getVcsInfo() throws ApiException {
        logger.info("\n--- Getting VCS Info ---");

        VcsInfo vcsInfo = api.vcsGet(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateNonNull(vcsInfo, "vcs info");
            validator.validateNonNull(vcsInfo.getBranch(), "branch");
        }

        logger.info("VCS Info retrieved successfully:");
        logger.info("  Branch: {}", vcsInfo.getBranch());
    }

    private void listWorktrees() throws ApiException {
        logger.info("\n--- Listing Worktrees ---");

        List<String> worktrees = api.worktreeList(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(worktrees, "worktrees");
        }

        logger.info("Found {} worktrees:", worktrees.size());
        for (String worktree : worktrees) {
            logger.info("  - {}", worktree);
        }
    }

    private Worktree createWorktree(String name) throws ApiException {
        logger.info("\n--- Creating Worktree: {} ---", name);

        try {
            WorktreeCreateInput input = new WorktreeCreateInput();
            input.setName(name);
            // startCommand is optional - leaving it null

            Worktree worktree = api.worktreeCreate(
                    null,   // directory
                    null,   // workspace
                    input
            );

            if (validator != null) {
                validator.validateNonNull(worktree, "created worktree");
                validator.validateNonNull(worktree.getName(), "worktree name");
            }

            logger.info("Worktree created successfully:");
            logger.info("  Name: {}", worktree.getName());
            logger.info("  Branch: {}", worktree.getBranch());
            logger.info("  Directory: {}", worktree.getDirectory());

            return worktree;
        } catch (ApiException e) {
            if (e.getCode() == 400 && e.getMessage().contains("WorktreeNotGitError")) {
                logger.warn("Worktree creation skipped: Not a git repository");
                return null;
            }
            throw e;
        }
    }

    private void removeWorktree(String directory) throws ApiException {
        logger.info("\n--- Removing Worktree: {} ---", directory);

        WorktreeRemoveInput input = new WorktreeRemoveInput();
        input.setDirectory(directory);

        Boolean result = api.worktreeRemove(
                null,   // directory
                null,   // workspace
                input
        );

        if (result) {
            logger.info("Worktree removed successfully");
        } else {
            logger.warn("Worktree removal returned false");
        }
    }

    public static void main(String[] args) {
        logger.info("Starting VCS Example");
        logger.info("====================");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));
        DefaultApi api = new DefaultApi(apiClient);

        try {
            // Run the example
            VcsExample example = new VcsExample(api);
            example.demonstrateVcsOperations();

            logger.info("\n");
            logger.info("====================");
            logger.info("Example completed successfully!");

        } catch (Exception e) {
            logger.error("Error running example: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
