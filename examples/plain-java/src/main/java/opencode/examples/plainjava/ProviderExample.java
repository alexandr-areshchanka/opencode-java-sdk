package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.ProviderApi;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Provider;
import opencode.sdk.model.ProviderAuthMethod;
import opencode.sdk.model.ProviderList200Response;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;
import java.util.Map;

public class ProviderExample {

    private static final Logger logger = LoggerFactory.getLogger(ProviderExample.class);

    private final ProviderApi providerApi;
    private final ResponseValidator validator;

    public ProviderExample(ExampleContext context) {
        this.providerApi = new ProviderApi(context.getApiClient());
        this.validator = context.getValidator();
    }

    public void demonstrateProviders() {
        try {
            logger.info("=== Provider Example ===");

            // List AI providers
            listProviders();

            // Get auth methods
            getAuthMethods();

            logger.info("=== Provider Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during provider operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during provider operations: {}", e.getMessage(), e);
        }
    }

    private void listProviders() throws ApiException {
        logger.info("\n--- Listing AI Providers ---");

        ProviderList200Response response = providerApi.providerList(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateNonNull(response, "provider list response");
        }

        List<Provider> allProviders = response.getAll();
        List<String> connectedProviders = response.getConnected();
        Map<String, String> defaults = response.getDefault();

        if (validator != null) {
            validator.validateCollection(allProviders, "all providers");
        }

        logger.info("Found {} providers:", allProviders.size());
        for (Provider provider : allProviders) {
            if (validator != null) {
                validator.validateNonNull(provider.getId(), "provider id");
                validator.validateNonNull(provider.getName(), "provider name");
            }

            logger.info("  - ID: {}", provider.getId());
            logger.info("    Name: {}", provider.getName());
            // TODO: Provider.getApi() and Provider.getNpm() fields were removed from the model
            logger.info("    Environment Variables: {}", provider.getEnv());
            logger.info("    Models: {} model(s)", provider.getModels().size());
        }

        if (!connectedProviders.isEmpty()) {
            logger.info("Connected providers: {}", connectedProviders);
        }

        if (!defaults.isEmpty()) {
            logger.info("Default provider mappings:");
            for (Map.Entry<String, String> entry : defaults.entrySet()) {
                logger.info("  {} -> {}", entry.getKey(), entry.getValue());
            }
        }
    }

    private void getAuthMethods() throws ApiException {
        logger.info("\n--- Getting Provider Auth Methods ---");

        Map<String, List<ProviderAuthMethod>> authMethods = providerApi.providerAuth(
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateNonNull(authMethods, "auth methods");
        }

        if (authMethods.isEmpty()) {
            logger.info("No auth methods configured.");
            return;
        }

        logger.info("Found auth methods for {} provider(s):", authMethods.size());
        for (Map.Entry<String, List<ProviderAuthMethod>> entry : authMethods.entrySet()) {
            String providerId = entry.getKey();
            List<ProviderAuthMethod> methods = entry.getValue();

            logger.info("  Provider: {}", providerId);
            for (ProviderAuthMethod method : methods) {
                if (validator != null) {
                    validator.validateNonNull(method.getType(), "auth method type");
                }

                logger.info("    - Type: {}", method.getType());
                logger.info("      Label: {}", method.getLabel());
            }
        }
    }

}
