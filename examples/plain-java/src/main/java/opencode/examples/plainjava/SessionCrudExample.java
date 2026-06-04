package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResourceTracker;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.GlobalApi;
import opencode.sdk.api.SessionApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.ExperimentalSessionListRootsParameter;
import opencode.sdk.model.Session;
import opencode.sdk.model.SessionCreateRequest;
import opencode.sdk.model.SessionUpdateRequest;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.math.BigDecimal;
import java.util.Base64;
import java.util.List;

public class SessionCrudExample {

    private static final Logger logger = LoggerFactory.getLogger(SessionCrudExample.class);

    private final SessionApi sessionApi;
    private final GlobalApi globalApi;
    private final ResponseValidator validator;
    private final ResourceTracker tracker;

    public SessionCrudExample(ApiClient apiClient) {
        this.sessionApi = new SessionApi(apiClient);
        this.globalApi = new GlobalApi(apiClient);
        this.validator = null;
        this.tracker = null;
    }

    public SessionCrudExample(ExampleContext context) {
        this.sessionApi = new SessionApi(context.getApiClient());
        this.globalApi = new GlobalApi(context.getApiClient());
        this.validator = context.getValidator();
        this.tracker = context.getResourceTracker();
    }

    public void demonstrateSessionCrud() {
        try {
            logger.info("=== Session CRUD Example ===");

            // List all sessions
            listSessions();

            // Create a new session
            String sessionId = createSession("Java SDK Example Session");
            logger.info("Created session with ID: {}", sessionId);

            // Get session by ID
            getSession(sessionId);

            // Update session title
            updateSession(sessionId, "Updated Java SDK Session Title");

            // Get the updated session
            getSession(sessionId);

            // Delete the session
            deleteSession(sessionId);

            logger.info("=== Session CRUD Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during session operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during session operations: {}", e.getMessage(), e);
        }
    }

    private void listSessions() throws ApiException {
        logger.info("\n--- Listing Sessions ---");

        List<Session> sessions = sessionApi.sessionList(
                null,  // directory
                null,  // workspace
                null,  // scope
                null,  // path
                new ExperimentalSessionListRootsParameter(),  // roots - only root sessions
                null,  // start - filter by timestamp
                null,  // search - filter by title
                new BigDecimal("10")  // limit - max 10 sessions
        );

        if (validator != null) {
            validator.validateCollection(sessions, "sessions");
        }

        logger.info("Found {} sessions:", sessions.size());
        for (Session session : sessions) {
            if (validator != null) {
                validator.validateNonNull(session.getId(), "session id");
            }

            logger.info("  - ID: {}, Title: {}",
                    session.getId(),
                    session.getTitle());
        }
    }

    private String createSession(String title) throws ApiException {
        logger.info("\n--- Creating Session ---");

        SessionCreateRequest request = new SessionCreateRequest();
        request.setTitle(title);

        Session session = sessionApi.sessionCreate(
                null,  // directory
                null,  // workspace
                request
        );

        if (validator != null) {
            validator.validateNonNull(session, "created session");
            validator.validateNonNull(session.getId(), "session id");
        }

        if (tracker != null) {
            tracker.trackSession(session.getId());
        }

        logger.info("Session created successfully");
        return session.getId();
    }

    private void getSession(String sessionId) throws ApiException {
        logger.info("\n--- Getting Session by ID: {} ---", sessionId);

        Session session = sessionApi.sessionGet(
                sessionId,
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateNonNull(session, "session");
            validator.validateNonNull(session.getId(), "session id");
            validator.validateNonNull(session.getTitle(), "session title");
        }

        logger.info("Session Details:");
        logger.info("  ID: {}", session.getId());
        logger.info("  Title: {}", session.getTitle());
        logger.info("  Slug: {}", session.getSlug());
        logger.info("  Created At: {}", session.getTime().getCreated());
        logger.info("  Updated At: {}", session.getTime().getUpdated());
    }

    private void updateSession(String sessionId, String newTitle) throws ApiException {
        logger.info("\n--- Updating Session: {} ---", sessionId);

        SessionUpdateRequest request = new SessionUpdateRequest();
        request.setTitle(newTitle);

        sessionApi.sessionUpdate(
                sessionId,
                null,  // directory
                null,  // workspace
                request
        );

        logger.info("Session updated successfully to: {}", newTitle);
    }

    private void deleteSession(String sessionId) throws ApiException {
        logger.info("\n--- Deleting Session: {} ---", sessionId);

        Boolean result = sessionApi.sessionDelete(
                sessionId,
                null,  // directory
                null   // workspace
        );

        if (result) {
            logger.info("Session deleted successfully");
        } else {
            logger.warn("Session deletion returned false");
        }
    }

    public static void main(String[] args) {
        logger.info("Starting Session CRUD Example");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));

        try {
            // Verify connection with health check
            GlobalApi globalApi = new GlobalApi(apiClient);
            var health = globalApi.globalHealth();
            logger.info("Connected to OpenCode server (version: {})", health.getVersion());

            // Run the example
            SessionCrudExample example = new SessionCrudExample(apiClient);
            example.demonstrateSessionCrud();

            logger.info("Session CRUD Example completed");

        } catch (ApiException e) {
            logger.error("Failed to connect to OpenCode server: {}", e.getMessage());
            System.exit(1);
        } catch (Exception e) {
            logger.error("Unexpected error: {}", e.getMessage(), e);
            System.exit(1);
        }
    }
}
