package opencode.examples.springboot.controller;

import opencode.examples.springboot.testsupport.AbstractIntegrationTest;
import opencode.sdk.model.Event;
import opencode.sdk.model.GlobalEvent;
import org.junit.jupiter.api.Test;
import org.springframework.http.ResponseEntity;

import static org.assertj.core.api.Assertions.assertThat;

class EventStreamingControllerIT extends AbstractIntegrationTest {

    @Test
    void shouldSubscribeToEvents() {
        ResponseEntity<Event> response =
                restTemplate.getForEntity("/api/events/", Event.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
    }

    @Test
    void shouldSubscribeToGlobalEvents() {
        ResponseEntity<GlobalEvent> response =
                restTemplate.getForEntity("/api/events/global", GlobalEvent.class);

        assertThat(response.getStatusCode().value()).isEqualTo(200);
        assertThat(response.getBody()).isNotNull();
        assertThat(response.getBody().getDirectory()).isNotNull();
        assertThat(response.getBody().getPayload()).isNotNull();
    }
}
