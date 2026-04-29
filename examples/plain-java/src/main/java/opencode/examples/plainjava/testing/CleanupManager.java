package opencode.examples.plainjava.testing;

import opencode.sdk.api.DefaultApi;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class CleanupManager {

    private static final Logger logger = LoggerFactory.getLogger(CleanupManager.class);

    private final DefaultApi api;
    private final TestLogger testLogger;

    public CleanupManager(DefaultApi api, TestLogger testLogger) {
        this.api = api;
        this.testLogger = testLogger;
    }

    public CleanupResult cleanup(ResourceTracker tracker) {
        List<TrackedResource> resources = tracker.getResources();
        int totalResources = resources.size();
        int cleanedResources = 0;
        int failedResources = 0;
        List<String> failures = new ArrayList<>();

        logger.info("Starting cleanup of {} resources", totalResources);

        for (TrackedResource resource : resources) {
            try {
                switch (resource.getType()) {
                    case "session":
                        cleanupSession(resource.getIdentifier());
                        break;
                    case "file":
                        cleanupFile(resource.getIdentifier());
                        break;
                    default:
                        logger.warn("Unknown resource type: {}", resource.getType());
                        failures.add(resource.getType() + ":" + resource.getIdentifier() + " (unknown type)");
                        failedResources++;
                        continue;
                }
                cleanedResources++;
                logger.debug("Cleaned up {} resource: {}", resource.getType(), resource.getIdentifier());
            } catch (Exception e) {
                handleCleanupFailure(resource.getType() + ":" + resource.getIdentifier(), e);
                failures.add(resource.getType() + ":" + resource.getIdentifier() + " (" + e.getMessage() + ")");
                failedResources++;
            }
        }

        logger.info("Cleanup completed: {}/{} resources cleaned, {} failed",
                cleanedResources, totalResources, failedResources);

        return new CleanupResult(totalResources, cleanedResources, failedResources, failures);
    }

    private void cleanupSession(String sessionId) {
        try {
            api.sessionDelete(sessionId, null, null);
            logger.debug("Deleted session: {}", sessionId);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete session: " + e.getMessage(), e);
        }
    }

    private void cleanupFile(String filePath) {
        try {
            // Note: OpenCode API doesn't have a file delete endpoint in the current spec
            // This is a placeholder for future implementation
            logger.debug("File cleanup not implemented for: {}", filePath);
        } catch (Exception e) {
            throw new RuntimeException("Failed to delete file: " + e.getMessage(), e);
        }
    }

    private void handleCleanupFailure(String resource, Exception e) {
        logger.warn("Failed to cleanup resource {}: {}", resource, e.getMessage());
        if (testLogger != null) {
            testLogger.logError("Cleanup failed for " + resource, e);
        }
    }
}
