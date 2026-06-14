package opencode.examples.plainjava.testing;

import opencode.sdk.api.PtyApi;
import opencode.sdk.api.SessionApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CleanupManager {

    private static final Logger logger = LoggerFactory.getLogger(CleanupManager.class);

    private final SessionApi sessionApi;
    private final PtyApi ptyApi;
    private final TestLogger testLogger;

    public CleanupManager(SessionApi sessionApi, PtyApi ptyApi, TestLogger testLogger) {
        this.sessionApi = sessionApi;
        this.ptyApi = ptyApi;
        this.testLogger = testLogger;
    }

    public CleanupResult cleanup(ResourceTracker tracker) {
        List<TrackedResource> resources = tracker.getResources();
        int totalResources = resources.size();
        int cleanedResources = 0;
        int skippedResources = 0;
        int failedResources = 0;
        List<String> failures = new ArrayList<>();

        logger.info("Starting cleanup of {} resources", totalResources);

        for (TrackedResource resource : resources) {
            try {
                switch (resource.getType()) {
                    case "session":
                        cleanupSession(resource.getIdentifier());
                        cleanedResources++;
                        logger.debug("Cleaned up {} resource: {}", resource.getType(), resource.getIdentifier());
                        break;
                    case "pty":
                        cleanupPty(resource.getIdentifier());
                        cleanedResources++;
                        logger.debug("Cleaned up {} resource: {}", resource.getType(), resource.getIdentifier());
                        break;
                    case "file":
                        cleanupFile(resource.getIdentifier());
                        skippedResources++;
                        logger.debug("Skipped {} resource (no delete endpoint): {}", resource.getType(), resource.getIdentifier());
                        break;
                    default:
                        logger.warn("Unknown resource type: {}", resource.getType());
                        failures.add(resource.getType() + ":" + resource.getIdentifier() + " (unknown type)");
                        failedResources++;
                        continue;
                }
            } catch (Exception e) {
                handleCleanupFailure(resource.getType() + ":" + resource.getIdentifier(), e);
                failures.add(resource.getType() + ":" + resource.getIdentifier() + " (" + e.getMessage() + ")");
                failedResources++;
            }
        }

        logger.info("Cleanup completed: {}/{} resources cleaned, {} skipped, {} failed",
                cleanedResources, totalResources, skippedResources, failedResources);

        return new CleanupResult(totalResources, cleanedResources, skippedResources, failedResources, failures);
    }

    private void cleanupSession(String sessionId) {
        try {
            sessionApi.sessionDelete(sessionId, null, null);
            logger.debug("Deleted session: {}", sessionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete session: " + e.getMessage(), e);
        }
    }

    private void cleanupPty(String ptyId) {
        try {
            ptyApi.ptyRemove(ptyId, null, null);
            logger.debug("Deleted pty: {}", ptyId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete pty: " + e.getMessage(), e);
        }
    }

    private void cleanupFile(String filePath) {
        // The OpenCode API exposes no file-delete endpoint, so file resources
        // cannot be removed server-side. Returning normally lets the caller
        // count the resource as skipped (a known limitation) rather than
        // falsely counting it as cleaned.
        logger.debug("No file delete endpoint available; skipping file resource: {}", filePath);
    }

    private void handleCleanupFailure(String resource, Exception e) {
        logger.warn("Failed to cleanup resource {}: {}", resource, e.getMessage());
        if (testLogger != null) {
            testLogger.logError("Cleanup failed for " + resource, e);
        }
    }
}
