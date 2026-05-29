package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResourceTracker;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.SessionApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;
import java.util.List;

public class SessionAdvancedExample {

    private static final Logger logger = LoggerFactory.getLogger(SessionAdvancedExample.class);

    private final SessionApi sessionApi;
    private final ResponseValidator validator;
    private final ResourceTracker tracker;

    public SessionAdvancedExample(ApiClient apiClient) {
        this.sessionApi = new SessionApi(apiClient);
        this.validator = null;
        this.tracker = null;
    }

    public SessionAdvancedExample(ExampleContext context) {
        this.sessionApi = new SessionApi(context.getApiClient());
        this.validator = context.getValidator();
        this.tracker = context.getResourceTracker();
    }

    public void demonstrateAdvancedSessionOperations() {
        try {
            logger.info("=== Session Advanced Operations Example ===");

            // Create a session for demonstrating advanced operations
            String sessionId = createSession("Advanced Operations Demo Session");
            logger.info("Created session with ID: {}", sessionId);

            // Demonstrate getting session details via SessionApi
            getSessionDetails(sessionId);

            // Demonstrate session forking
            forkSession(sessionId);

            // Demonstrate getting session children via SessionApi
            getSessionChildren(sessionId);

            // Demonstrate session sharing
            shareSession(sessionId);

            // Demonstrate session unsharing
            unshareSession(sessionId);

            // Demonstrate session summarization
            summarizeSession(sessionId);

            // Demonstrate session abort
            abortSession(sessionId);

            // Demonstrate session revert
            revertSession(sessionId);

            // Demonstrate session unrevert
            unrevertSession(sessionId);

            logger.info("=== Session Advanced Operations Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during advanced session operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during advanced session operations: {}", e.getMessage(), e);
        }
    }

    private String createSession(String title) throws ApiException {
        logger.info("\n--- Creating Session ---");

        SessionCreateRequest request = new SessionCreateRequest();
        request.setTitle(title);

        Session session = sessionApi.sessionCreate(
                null,
                null,
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

    private void forkSession(String sessionId) throws ApiException {
        logger.info("\n--- Forking Session: {} ---", sessionId);

        SessionForkRequest request = new SessionForkRequest();
        request.setMessageID(null);

        Session forkedSession = sessionApi.sessionFork(
                sessionId,
                null,
                null,
                request
        );

        if (validator != null) {
            validator.validateNonNull(forkedSession, "forked session");
            validator.validateNonNull(forkedSession.getId(), "forked session id");
        }

        if (tracker != null) {
            tracker.trackSession(forkedSession.getId());
        }

        logger.info("Session forked successfully. New session ID: {}", forkedSession.getId());
    }

    private void getSessionDetails(String sessionId) throws ApiException {
        logger.info("\n--- Getting Session Details via SessionApi: {} ---", sessionId);

        Session session = sessionApi.sessionGet(sessionId, null, null);

        logger.info("Session retrieved successfully");
        logger.info("  ID: {}", session.getId());
        logger.info("  Title: {}", session.getTitle());
        logger.info("  Directory: {}", session.getDirectory());
        if (session.getTime() != null) {
            logger.info("  Time: created={}, updated={}",
                    session.getTime().getCreated(),
                    session.getTime().getUpdated());
        }
        if (session.getSummary() != null) {
            logger.info("  Summary: additions={}, deletions={}, files={}",
                    session.getSummary().getAdditions(),
                    session.getSummary().getDeletions(),
                    session.getSummary().getFiles());
        }
    }

    private void getSessionChildren(String sessionId) throws ApiException {
        logger.info("\n--- Getting Session Children via SessionApi: {} ---", sessionId);

        List<Session> children = sessionApi.sessionChildren(sessionId, null, null);

        if (children != null && !children.isEmpty()) {
            logger.info("Found {} child session(s):", children.size());
            for (Session child : children) {
                logger.info("  - Child ID: {}, Title: {}", child.getId(), child.getTitle());
            }
        } else {
            logger.info("No child sessions found");
        }
    }

    private void shareSession(String sessionId) throws ApiException {
        logger.info("\n--- Sharing Session: {} ---", sessionId);

        Session session = sessionApi.sessionShare(
                sessionId,
                null,
                null
        );

        if (session.getShare() != null) {
            logger.info("Session shared successfully. Share URL: {}", session.getShare().getUrl());
        } else {
            logger.info("Session shared successfully");
        }
    }

    private void unshareSession(String sessionId) throws ApiException {
        logger.info("\n--- Unsharing Session: {} ---", sessionId);

        Session session = sessionApi.sessionUnshare(
                sessionId,
                null,
                null
        );

        logger.info("Session unshared successfully");
    }

    private void summarizeSession(String sessionId) throws ApiException {
        logger.info("\n--- Summarizing Session: {} ---", sessionId);

        SessionSummarizeRequest request = new SessionSummarizeRequest();
        request.setProviderID("z.ai");
        request.setModelID("glm-4.7");
        request.setAuto(true);

        Boolean result = sessionApi.sessionSummarize(
                sessionId,
                null,
                null,
                request
        );

        if (result) {
            logger.info("Session summarization initiated successfully");
        } else {
            logger.warn("Session summarization returned false");
        }
    }

    private void abortSession(String sessionId) throws ApiException {
        logger.info("\n--- Aborting Session Processing: {} ---", sessionId);

        Boolean result = sessionApi.sessionAbort(
                sessionId,
                null,
                null
        );

        if (result) {
            logger.info("Session aborted successfully");
        } else {
            logger.warn("Session abort returned false");
        }
    }

    private void revertSession(String sessionId) throws ApiException {
        logger.info("\n--- Reverting Session: {} ---", sessionId);

        SessionRevertRequest request = new SessionRevertRequest();
        request.setMessageID("message-id-to-revert-to");
        request.setPartID(null);

        Session session = sessionApi.sessionRevert(
                sessionId,
                null,
                null,
                request
        );

        logger.info("Session reverted successfully");
    }

    private void unrevertSession(String sessionId) throws ApiException {
        logger.info("\n--- Unreverting Session: {} ---", sessionId);

        Session session = sessionApi.sessionUnrevert(
                sessionId,
                null,
                null
        );

        logger.info("Session unreverted successfully");
    }

    public static void main(String[] args) {
        try {
            // Configure the client
            ApiClient apiClient = new ApiClient();
            apiClient.updateBaseUri("http://localhost:4096");
            String credentials = "opencode:opencode123";
            String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
            apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));

            SessionAdvancedExample example = new SessionAdvancedExample(apiClient);
            example.demonstrateAdvancedSessionOperations();

        } catch (Exception e) {
            System.err.println("Fatal error: " + e.getMessage());
            e.printStackTrace();
        }
    }
}
