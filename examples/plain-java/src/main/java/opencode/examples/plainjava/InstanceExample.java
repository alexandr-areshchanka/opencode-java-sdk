package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.invoker.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Base64;

public class InstanceExample {

    private static final Logger logger = LoggerFactory.getLogger(InstanceExample.class);

    private final ResponseValidator validator;

    public InstanceExample(ApiClient apiClient) {
        this.validator = null;
    }

    public InstanceExample(ExampleContext context) {
        this.validator = context.getValidator();
    }

    public void demonstrateInstanceManagement() {
        try {
            logger.info("=== Instance Management Example ===");

            // IMPORTANT WARNING about destructive operations
            logger.warn("**************************************************************************");
            logger.warn("*                                                                        *");
            logger.warn("*  WARNING: The following operations are DESTRUCTIVE!                    *");
            logger.warn("*                                                                        *");
            logger.warn("*  This example demonstrates instance disposal APIs that will:          *");
            logger.warn("*  - Terminate active sessions                                           *");
            logger.warn("*  - Release all resources associated with the instance(s)               *");
            logger.warn("*  - Potentially disrupt ongoing work                                  *");
            logger.warn("*                                                                        *");
            logger.warn("*  In a production environment, use these APIs with extreme caution.   *");
            logger.warn("*                                                                        *");
            logger.warn("**************************************************************************");

            // Demonstrate instanceDispose API (without actually calling it)
            explainInstanceDispose();

            // Demonstrate globalDispose API (without actually calling it)
            explainGlobalDispose();

            logger.info("=== Instance Management Example Completed Successfully ===");
            logger.info("Note: No actual disposal was performed to avoid disrupting the server.");

        } catch (Exception e) {
            logger.error("Error during instance management demonstration: {}", e.getMessage(), e);
        }
    }

    private void explainInstanceDispose() {
        logger.info("\n--- instanceDispose() API ---");
        logger.info("Description: Clean up and dispose the current OpenCode instance.");
        logger.info("Parameters:");
        logger.info("  - directory (optional): The project directory");
        logger.info("  - workspace (optional): The workspace identifier");
        logger.info("Returns: Boolean indicating success");
        logger.info("");
        logger.info("This operation:");
        logger.info("  - Disposes the current instance for the specified directory/workspace");
        logger.info("  - Releases resources associated with that specific instance");
        logger.info("  - Terminates any active sessions within that instance");
        logger.info("");
        logger.info("Example usage:");
        logger.info("  InstanceApi instanceApi = new InstanceApi(apiClient);");
        logger.info("  Boolean result = instanceApi.instanceDispose(null, null);");
        logger.info("  if (result) {");
        logger.info("      logger.info(\"Instance disposed successfully\");");
        logger.info("  }");
    }

    private void explainGlobalDispose() {
        logger.info("\n--- globalDispose() API ---");
        logger.info("Description: Clean up and dispose ALL OpenCode instances.");
        logger.info("Parameters: None");
        logger.info("Returns: Boolean indicating success");
        logger.info("");
        logger.info("This operation:");
        logger.info("  - Disposes ALL running OpenCode instances");
        logger.info("  - Releases all resources across the entire server");
        logger.info("  - Terminates ALL active sessions system-wide");
        logger.info("");
        logger.warn("⚠️  This is the most DESTRUCTIVE operation and should be used with extreme caution!");
        logger.info("");
        logger.info("Example usage:");
        logger.info("  GlobalApi globalApi = new GlobalApi(apiClient);");
        logger.info("  Boolean result = globalApi.globalDispose();");
        logger.info("  if (result) {");
        logger.info("      logger.info(\"All instances disposed successfully\");");
        logger.info("  }");
    }

    public static void main(String[] args) {
        logger.info("Starting Instance Management Example");
        logger.info("======================================");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));

        try {
            // Run the example
            InstanceExample example = new InstanceExample(apiClient);
            example.demonstrateInstanceManagement();

            logger.info("\n");
            logger.info("======================================");
            logger.info("Example completed successfully!");

        } catch (Exception e) {
            logger.error("Error running example: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
