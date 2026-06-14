package opencode.examples.springboot.controller;

import opencode.examples.springboot.testsupport.AbstractIntegrationTest;
import opencode.sdk.model.GlobalHealth200Response;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class InstanceControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldListInstances() {
        ResponseEntity<GlobalHealth200Response> response =
                restTemplate.getForEntity("/api/instances", GlobalHealth200Response.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getVersion()).isNotBlank();
        assertThat(response.getBody().getHealthy()).isNotNull();
    }

    @Test
    void shouldReturnNotImplementedForCreateInstance() {
        ResponseEntity<String> response =
                restTemplate.postForEntity("/api/instances", null, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(501);
    }

    @Test
    void shouldHandleDisposeInstance() {
        ResponseEntity<String> response =
                restTemplate.exchange("/api/instances/test-instance", org.springframework.http.HttpMethod.DELETE, null, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
    }
}
