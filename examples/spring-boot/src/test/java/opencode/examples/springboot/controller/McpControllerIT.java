package opencode.examples.springboot.controller;

import opencode.examples.springboot.testsupport.AbstractIntegrationTest;
import opencode.sdk.model.MCPStatus;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

class McpControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldListMcpServers() {
        ResponseEntity<Map<String, MCPStatus>> response =
                restTemplate.exchange("/api/mcp", HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldReturnNotFoundForUnknownServerAuthStart() {
        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/mcp/definitely-not-a-real-server/auth/start", null, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
