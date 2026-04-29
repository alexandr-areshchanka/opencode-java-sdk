package opencode.examples.plainjava.testing;

import opencode.sdk.api.DefaultApi;
import opencode.sdk.invoker.ApiClient;

public class ExampleContext {

    private final DefaultApi defaultApi;
    private final ApiClient apiClient;
    private final TestConfiguration config;
    private final ResourceTracker resourceTracker;
    private final ResponseValidator validator;

    public ExampleContext(DefaultApi defaultApi, ApiClient apiClient, TestConfiguration config,
                          ResourceTracker resourceTracker, ResponseValidator validator) {
        this.defaultApi = defaultApi;
        this.apiClient = apiClient;
        this.config = config;
        this.resourceTracker = resourceTracker;
        this.validator = validator;
    }

    public DefaultApi getDefaultApi() {
        return defaultApi;
    }

    public ApiClient getApiClient() {
        return apiClient;
    }

    public TestConfiguration getConfig() {
        return config;
    }

    public ResourceTracker getResourceTracker() {
        return resourceTracker;
    }

    public ResponseValidator getValidator() {
        return validator;
    }
}
