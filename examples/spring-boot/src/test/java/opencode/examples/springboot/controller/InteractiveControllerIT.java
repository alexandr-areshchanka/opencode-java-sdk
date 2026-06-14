package opencode.examples.springboot.controller;

import opencode.examples.springboot.testsupport.AbstractIntegrationTest;
import opencode.sdk.model.PermissionRequest;
import opencode.sdk.model.QuestionRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InteractiveControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldListQuestions() {
        ResponseEntity<List<QuestionRequest>> response =
                restTemplate.exchange("/api/interactive/questions", HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldListPermissions() {
        ResponseEntity<List<PermissionRequest>> response =
                restTemplate.exchange("/api/interactive/permissions", HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldRejectReplyWithBlankRequestId() {
        String requestBody = "{\"answers\": []}";

        ResponseEntity<String> response =
                restTemplate.postForEntity(
                        "/api/interactive/questions/reply?requestId=", requestBody, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(400);
    }
}
