package opencode.sdk.springboot.autoconfigure;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import opencode.sdk.invoker.ApiClient;
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
@ConditionalOnClass(ApiClient.class)
@EnableConfigurationProperties(OpenCodeProperties.class)
public class OpenCodeAutoConfiguration {

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

        if (properties.getUsername() != null && properties.getPassword() != null) {
            String authHeader = createBasicAuthHeader(properties.getUsername(), properties.getPassword());
            client.setRequestInterceptor(builder -> builder.header("Authorization", authHeader));
        }

        return client;
    }

    @Bean
    @ConditionalOnMissingBean
    public opencode.sdk.springboot.OpenCodeService openCodeService(ApiClient apiClient) {
        return new opencode.sdk.springboot.OpenCodeService(apiClient);
    }

    private String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }
}
