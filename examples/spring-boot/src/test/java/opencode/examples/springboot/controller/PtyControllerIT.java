package opencode.examples.springboot.controller;

import opencode.examples.springboot.testsupport.AbstractIntegrationTest;
import opencode.sdk.model.Pty;
import opencode.sdk.model.PtyCreateRequest;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import org.springframework.core.ParameterizedTypeReference;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PtyControllerIT extends AbstractIntegrationTest {

    private final List<String> createdPtyIds = new ArrayList<>();

    @AfterEach
    void cleanupCreatedPtys() {
        List<String> ids = new ArrayList<>(createdPtyIds);
        createdPtyIds.clear();
        for (String ptyId : ids) {
            try {
                restTemplate.exchange("/api/pty/" + ptyId, HttpMethod.DELETE, null, String.class);
            } catch (Exception ignored) {
                // best-effort cleanup
            }
        }
    }

    @Test
    void shouldListPtys() {
        ResponseEntity<List<Pty>> response =
                restTemplate.exchange("/api/pty", HttpMethod.GET, null,
                        new ParameterizedTypeReference<>() {
                        });

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        response.getBody().forEach(pty -> assertThat(pty.getId()).isNotBlank());
    }

    @Test
    void shouldCreateAndDeletePty() {
        PtyCreateRequest request = new PtyCreateRequest();
        request.setCommand("echo");
        request.addArgsItem("hello");
        request.setTitle("it-test-pty");

        ResponseEntity<Pty> response =
                restTemplate.postForEntity("/api/pty", request, Pty.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getId()).isNotBlank();
        assertThat(response.getBody().getTitle()).isEqualTo("it-test-pty");
        assertThat(response.getBody().getCommand()).isEqualTo("echo");
        assertThat(response.getBody().getStatus()).isNotNull();

        String ptyId = response.getBody().getId();
        createdPtyIds.add(ptyId);

        ResponseEntity<Boolean> deleteResponse = restTemplate.exchange(
                "/api/pty/" + ptyId, HttpMethod.DELETE, null, Boolean.class);

        assertThat(deleteResponse.getStatusCode().value()).isEqualTo(200);
    }
}
