package opencode.examples.plainjava.testing;

import opencode.sdk.invoker.ApiClient;

public class ExampleContext {

    private final ApiClient apiClient;
    private final TestConfiguration config;
    private final ResourceTracker resourceTracker;
    private final ResponseValidator validator;

    public ExampleContext(ApiClient apiClient, TestConfiguration config,
                          ResourceTracker resourceTracker, ResponseValidator validator) {
        this.apiClient = apiClient;
        this.config = config;
        this.resourceTracker = resourceTracker;
        this.validator = validator;
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
