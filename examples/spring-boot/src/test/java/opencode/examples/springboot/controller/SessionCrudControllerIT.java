package opencode.examples.springboot.controller;

import opencode.examples.springboot.testsupport.AbstractIntegrationTest;
import opencode.sdk.model.Session;
import opencode.sdk.model.SessionCreateRequest;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SessionCrudControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldListSessions() {
        ResponseEntity<List<Session>> response =
                restTemplate.exchange("/api/sessions", HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        response.getBody().forEach(session -> assertThat(session.getId()).isNotBlank());
    }

    @Test
    void shouldCreateSession() {
        SessionCreateRequest request = new SessionCreateRequest();
        request.setTitle("Test Session");

        ResponseEntity<Session> response =
                restTemplate.postForEntity("/api/sessions", request, Session.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotBlank();
        assertThat(response.getBody().getTitle()).isEqualTo("Test Session");
        trackSession(response.getBody().getId());
    }

    @Test
    void shouldReturnNotFoundForInvalidSessionId() {
        ResponseEntity<String> response =
                restTemplate.getForEntity("/api/sessions/invalid-id", String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }

    @Test
    void shouldHandleDeleteForInvalidSessionId() {
        ResponseEntity<String> response =
                restTemplate.exchange("/api/sessions/invalid-id", HttpMethod.DELETE, null, String.class);

        assertThat(response.getStatusCode().value()).isEqualTo(404);
    }
}
