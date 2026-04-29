package opencode.sdk.springboot;

import opencode.sdk.api.DefaultApi;
import opencode.sdk.api.SessionApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.GlobalHealth200Response;
import org.springframework.stereotype.Service;

@Service
public class OpenCodeService {

    private final DefaultApi defaultApi;
    private final ApiClient apiClient;

    public OpenCodeService(DefaultApi defaultApi, ApiClient apiClient) {
        this.defaultApi = defaultApi;
        this.apiClient = apiClient;
    }

    public GlobalHealth200Response getHealth() throws ApiException {
        return defaultApi.globalHealth();
    }

    public DefaultApi api() {
        return defaultApi;
    }

    public SessionApi sessionApi() {
        return new SessionApi(apiClient);
    }
}
