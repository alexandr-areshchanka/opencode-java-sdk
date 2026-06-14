package opencode.examples.plainjava.testing;

import opencode.sdk.invoker.ApiClient;

import java.util.Base64;
import java.util.Objects;

public final class SdkClientFactory {

    private SdkClientFactory() {
    }

    public static ApiClient createClient(String baseUrl, String username, String password) {
        Objects.requireNonNull(baseUrl, "baseUrl must not be null");

        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri(baseUrl);

        if (username != null && password != null) {
            apiClient.setRequestInterceptor(
                    builder -> builder.header("Authorization", basicAuthHeader(username, password)));
        }

        return apiClient;
    }

    private static String basicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }
}
