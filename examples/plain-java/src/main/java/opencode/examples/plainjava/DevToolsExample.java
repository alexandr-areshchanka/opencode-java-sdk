package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.InstanceApi;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.FormatterStatus;
import opencode.sdk.model.LSPStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class DevToolsExample {

    private static final Logger logger = LoggerFactory.getLogger(DevToolsExample.class);

    private final InstanceApi instanceApi;
    private final ResponseValidator validator;

    public DevToolsExample(ExampleContext context) {
        this.instanceApi = new InstanceApi(context.getApiClient());
        this.validator = context.getValidator();
    }

    public void demonstrateDevTools() {
        try {
            logger.info("=== DevTools Example ===");

            // Get LSP server status
            getLspStatus();

            // Get formatter status
            getFormatterStatus();

            logger.info("=== DevTools Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during DevTools operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during DevTools operations: {}", e.getMessage(), e);
        }
    }

    private void getLspStatus() throws ApiException {
        logger.info("\n--- Getting LSP Server Status ---");

        List<LSPStatus> lspStatuses = instanceApi.lspStatus(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(lspStatuses, "lsp statuses");
        }

        logger.info("Found {} LSP server(s):", lspStatuses.size());
        for (LSPStatus status : lspStatuses) {
            if (validator != null) {
                validator.validateNonNull(status.getId(), "lsp id");
                validator.validateNonNull(status.getName(), "lsp name");
            }

            logger.info("  - ID: {}", status.getId());
            logger.info("    Name: {}", status.getName());
            logger.info("    Root: {}", status.getRoot());
            logger.info("    Status: {}", status.getStatus());
        }
    }

    private void getFormatterStatus() throws ApiException {
        logger.info("\n--- Getting Formatter Status ---");

        List<FormatterStatus> formatterStatuses = instanceApi.formatterStatus(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(formatterStatuses, "formatter statuses");
        }

        logger.info("Found {} formatter(s):", formatterStatuses.size());
        for (FormatterStatus status : formatterStatuses) {
            if (validator != null) {
                validator.validateNonNull(status.getName(), "formatter name");
            }

            logger.info("  - Name: {}", status.getName());
            logger.info("    Extensions: {}", status.getExtensions());
            logger.info("    Enabled: {}", status.getEnabled());
        }
    }

}
