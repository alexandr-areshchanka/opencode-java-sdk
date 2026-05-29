package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResourceTracker;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.PtyApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Pty;
import opencode.sdk.model.PtyCreateRequest;
import opencode.sdk.model.PtyUpdateRequest;
import opencode.sdk.model.PtyUpdateRequestSize;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;

public class PtyExample {

    private static final Logger logger = LoggerFactory.getLogger(PtyExample.class);

    private final PtyApi ptyApi;
    private final ResponseValidator validator;
    private final ResourceTracker tracker;

    public PtyExample(ApiClient apiClient) {
        this.ptyApi = new PtyApi(apiClient);
        this.validator = null;
        this.tracker = null;
    }

    public PtyExample(ExampleContext context) {
        this.ptyApi = new PtyApi(context.getApiClient());
        this.validator = context.getValidator();
        this.tracker = context.getResourceTracker();
    }

    public void demonstratePtyOperations() {
        try {
            logger.info("=== PTY (Pseudo-Terminal) Example ===");

            // List existing PTY sessions
            listPtySessions();

            // Create a new PTY session
            Pty pty = createPtySession();
            if (pty != null) {
                String ptyId = pty.getId();

                // Get PTY details
                getPty(ptyId);

                // Resize the PTY terminal
                resizePty(ptyId, 40, 120);

                // Update PTY title
                updatePtyTitle(ptyId, "Updated PTY Session");

                // List PTY sessions again to see our new session
                listPtySessions();

                // Remove (kill) the PTY session
                removePty(ptyId);
            }

            logger.info("=== PTY Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during PTY operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during PTY operations: {}", e.getMessage(), e);
        }
    }

    private void listPtySessions() throws ApiException {
        logger.info("\n--- Listing PTY Sessions ---");

        List<Pty> ptys = ptyApi.ptyList(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(ptys, "pty sessions");
        }

        logger.info("Found {} PTY sessions:", ptys.size());
        for (Pty pty : ptys) {
            if (validator != null) {
                validator.validateNonNull(pty.getId(), "pty id");
            }

            logger.info("  - ID: {}", pty.getId());
            logger.info("    Title: {}", pty.getTitle());
            logger.info("    Command: {}", pty.getCommand());
            logger.info("    Status: {}", pty.getStatus());
            logger.info("    PID: {}", pty.getPid());
        }
    }

    private Pty createPtySession() throws ApiException {
        logger.info("\n--- Creating PTY Session ---");

        PtyCreateRequest request = new PtyCreateRequest();
        request.setCommand("bash");
        request.setTitle("Example PTY Session");
        request.setCwd(System.getProperty("user.dir"));

        Pty pty = ptyApi.ptyCreate(
                null,     // directory
                null,     // workspace
                request
        );

        if (validator != null) {
            validator.validateNonNull(pty, "created pty");
            validator.validateNonNull(pty.getId(), "pty id");
        }

        if (tracker != null) {
            tracker.trackResource("pty", pty.getId());
        }

        logger.info("PTY session created successfully:");
        logger.info("  ID: {}", pty.getId());
        logger.info("  Title: {}", pty.getTitle());
        logger.info("  Command: {}", pty.getCommand());
        logger.info("  Args: {}", pty.getArgs());
        logger.info("  CWD: {}", pty.getCwd());
        logger.info("  Status: {}", pty.getStatus());
        logger.info("  PID: {}", pty.getPid());

        return pty;
    }

    private void getPty(String ptyId) throws ApiException {
        logger.info("\n--- Getting PTY Details: {} ---", ptyId);

        Pty pty = ptyApi.ptyGet(
                ptyId,
                null,  // directory
                null   // workspace
        );

        logger.info("PTY details retrieved:");
        logger.info("  ID: {}", pty.getId());
        logger.info("  Title: {}", pty.getTitle());
        logger.info("  Status: {}", pty.getStatus());
    }

    private void resizePty(String ptyId, int rows, int cols) throws ApiException {
        logger.info("\n--- Resizing PTY: {} to {}x{} ---", ptyId, rows, cols);

        PtyUpdateRequestSize size = new PtyUpdateRequestSize();
        size.setRows(rows);
        size.setCols(cols);

        PtyUpdateRequest request = new PtyUpdateRequest();
        request.setSize(size);

        Pty pty = ptyApi.ptyUpdate(
                ptyId,
                null,     // directory
                null,     // workspace
                request
        );

        logger.info("PTY resized successfully:");
        logger.info("  ID: {}", pty.getId());
        logger.info("  Title: {}", pty.getTitle());
    }

    private void updatePtyTitle(String ptyId, String title) throws ApiException {
        logger.info("\n--- Updating PTY Title: {} ---", ptyId);

        PtyUpdateRequest request = new PtyUpdateRequest();
        request.setTitle(title);

        Pty pty = ptyApi.ptyUpdate(
                ptyId,
                null,     // directory
                null,     // workspace
                request
        );

        logger.info("PTY title updated successfully:");
        logger.info("  ID: {}", pty.getId());
        logger.info("  New Title: {}", pty.getTitle());
    }

    private void removePty(String ptyId) throws ApiException {
        logger.info("\n--- Removing PTY Session: {} ---", ptyId);

        Boolean result = ptyApi.ptyRemove(
                ptyId,
                null,  // directory
                null   // workspace
        );

        if (result) {
            logger.info("PTY session removed successfully");
        } else {
            logger.warn("PTY removal returned false");
        }
    }

    public static void main(String[] args) {
        logger.info("Starting PTY Example");
        logger.info("====================");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));

        try {
            // Run the example
            PtyExample example = new PtyExample(apiClient);
            example.demonstratePtyOperations();

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
