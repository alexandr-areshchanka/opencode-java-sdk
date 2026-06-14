package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.ConfigApi;
import opencode.sdk.api.GlobalApi;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Config;
import opencode.sdk.model.ConfigProviders200Response;
import opencode.sdk.model.LogLevel;
import opencode.sdk.model.Provider;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ConfigurationExample {

    private static final Logger logger = LoggerFactory.getLogger(ConfigurationExample.class);

    private final ConfigApi configApi;
    private final GlobalApi globalApi;
    private final ResponseValidator validator;

    public ConfigurationExample(ExampleContext context) {
        this.configApi = new ConfigApi(context.getApiClient());
        this.globalApi = new GlobalApi(context.getApiClient());
        this.validator = context.getValidator();
    }

    public void demonstrateConfiguration() {
        try {
            logger.info("=== Configuration Example ===");

            // Get project config
            getProjectConfig();

            // Get global config
            getGlobalConfig();

            // List providers
            listProviders();

            // Update config (demonstration only - won't actually save)
            demonstrateConfigUpdate();

            logger.info("=== Configuration Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during configuration operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during configuration operations: {}", e.getMessage(), e);
        }
    }

    private void getProjectConfig() throws ApiException {
        logger.info("\n--- Retrieving Project Configuration ---");

        Config config = configApi.configGet(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateNonNull(config, "project config");
        }

        logger.info("Project config retrieved successfully!");
        if (config.getModel() != null) {
            logger.info("  Model: {}", config.getModel());
        }
        if (config.getSmallModel() != null) {
            logger.info("  Small Model: {}", config.getSmallModel());
        }
        if (config.getDefaultAgent() != null) {
            logger.info("  Default Agent: {}", config.getDefaultAgent());
        }
        if (config.getLogLevel() != null) {
            logger.info("  Log Level: {}", config.getLogLevel());
        }
        if (config.getShare() != null) {
            logger.info("  Share Setting: {}", config.getShare());
        }
    }

    private void getGlobalConfig() throws ApiException {
        logger.info("\n--- Retrieving Global Configuration ---");

        Config config = globalApi.globalConfigGet();

        if (validator != null) {
            validator.validateNonNull(config, "global config");
        }

        logger.info("Global config retrieved successfully!");
        if (config.getServer() != null) {
            logger.info("  Server Config: {}", config.getServer());
        }
        if (config.getUsername() != null) {
            logger.info("  Username: {}", config.getUsername());
        }
        if (config.getSnapshot() != null) {
            logger.info("  Snapshot Enabled: {}", config.getSnapshot());
        }
        if (config.getAutoshare() != null) {
            logger.info("  Autoshare: {}", config.getAutoshare());
        }
    }

    private void listProviders() throws ApiException {
        logger.info("\n--- Listing Configuration Providers ---");

        ConfigProviders200Response response = configApi.configProviders(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateNonNull(response, "providers response");
        }

        List<Provider> providers = response.getProviders();
        Map<String, String> defaults = response.getDefault();

        if (validator != null) {
            validator.validateCollection(providers, "providers");
        }

        logger.info("Found {} providers:", providers.size());
        for (Provider provider : providers) {
            if (validator != null) {
                validator.validateNonNull(provider.getId(), "provider id");
                validator.validateNonNull(provider.getName(), "provider name");
            }

            logger.info("  - ID: {}", provider.getId());
            logger.info("    Name: {}", provider.getName());
            logger.info("    Source: {}", provider.getSource());
            if (provider.getKey() != null) {
                logger.info("    Key: {}", provider.getKey());
            }
            logger.info("    Models: {} model(s)", provider.getModels().size());
        }

        if (!defaults.isEmpty()) {
            logger.info("Default provider mappings:");
            for (Map.Entry<String, String> entry : defaults.entrySet()) {
                logger.info("  {} -> {}", entry.getKey(), entry.getValue());
            }
        }
    }

    private void demonstrateConfigUpdate() throws ApiException {
        logger.info("\n--- Demonstrating Config Update (PATCH) ---");

        // Create a config update with minimal changes
        Config configUpdate = new Config();
        configUpdate.setLogLevel(LogLevel.DEBUG);

        Config updatedConfig = configApi.configUpdate(
                null,           // directory
                null,           // workspace
                configUpdate    // config changes
        );

        if (validator != null) {
            validator.validateNonNull(updatedConfig, "updated config");
        }

        logger.info("Config updated successfully!");
        if (updatedConfig.getLogLevel() != null) {
            logger.info("  New Log Level: {}", updatedConfig.getLogLevel());
        }
    }

}
