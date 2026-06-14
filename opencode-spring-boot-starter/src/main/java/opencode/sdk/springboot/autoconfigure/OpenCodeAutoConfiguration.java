package opencode.sdk.springboot.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import opencode.sdk.invoker.ApiClient;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;
import java.util.Locale;

@Configuration
@ConditionalOnClass(ApiClient.class)
@EnableConfigurationProperties(OpenCodeProperties.class)
public class OpenCodeAutoConfiguration {

    private static final Logger log = LoggerFactory.getLogger(OpenCodeAutoConfiguration.class);

    @Bean
    @ConditionalOnMissingBean
    public ApiClient apiClient(OpenCodeProperties properties) {
        ApiClient client = new ApiClient();

        // Use NON_EMPTY so default-initialized fields (e.g. empty lists) are not serialized
        ObjectMapper mapper = ApiClient.createDefaultObjectMapper();
        mapper.setSerializationInclusion(JsonInclude.Include.NON_EMPTY);
        client.setObjectMapper(mapper);

        if (properties.getBaseUrl() != null) {
            client.updateBaseUri(properties.getBaseUrl());
        }

        if (properties.getConnectTimeout() != null) {
            client.setConnectTimeout(properties.getConnectTimeout());
        }

        if (properties.getReadTimeout() != null) {
            client.setReadTimeout(properties.getReadTimeout());
        }

        boolean authApplied = wireAuth(client, properties);

        if (authApplied && properties.getBaseUrl() != null
                && !properties.getBaseUrl().toLowerCase(Locale.ROOT).startsWith("https://")) {
            log.warn("opencode.base-url '{}' is not HTTPS; authentication credentials will be sent over an unencrypted connection. Set an https base-url or disable auth (opencode.auth.type=none).",
                    properties.getBaseUrl());
        }

        return client;
    }

    @Bean
    @ConditionalOnMissingBean
    public opencode.sdk.springboot.OpenCodeService openCodeService(ApiClient apiClient) {
        return new opencode.sdk.springboot.OpenCodeService(apiClient);
    }

    private boolean wireAuth(ApiClient client, OpenCodeProperties properties) {
        String authType = "basic";
        if (properties.getAuth() != null
                && properties.getAuth().getType() != null
                && !properties.getAuth().getType().isBlank()) {
            authType = properties.getAuth().getType().trim().toLowerCase(Locale.ROOT);
        }

        switch (authType) {
            case "none":
                return false;
            case "bearer":
                return wireBearerAuth(client, properties.getBearerToken());
            case "basic":
            default:
                return wireBasicAuth(client, properties.getUsername(), properties.getPassword());
        }
    }

    private boolean wireBearerAuth(ApiClient client, String bearerToken) {
        if (bearerToken == null || bearerToken.isEmpty()) {
            return false;
        }
        client.setRequestInterceptor(builder -> builder.header("Authorization", "Bearer " + bearerToken));
        return true;
    }

    private boolean wireBasicAuth(ApiClient client, String username, String password) {
        if (username == null || password == null) {
            return false;
        }
        String authHeader = createBasicAuthHeader(username, password);
        client.setRequestInterceptor(builder -> builder.header("Authorization", authHeader));
        return true;
    }

    private String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }
}
