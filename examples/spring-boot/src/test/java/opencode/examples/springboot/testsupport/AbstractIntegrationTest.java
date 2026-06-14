package opencode.examples.springboot.testsupport;

import org.junit.jupiter.api.AfterEach;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.ArrayList;
import java.util.List;

@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@Testcontainers
@ActiveProfiles("test")
@AutoConfigureTestRestTemplate
public abstract class AbstractIntegrationTest {

    @Container
    protected static final OpenCodeServerContainer OPENCODE_CONTAINER = 
        new OpenCodeServerContainer()
            .withReuse(!isCiEnvironment() && Boolean.parseBoolean(
                System.getProperty("testcontainers.reuse.enable", "false")
            ));

    @Autowired
    protected TestRestTemplate restTemplate;

    private final List<String> createdSessionIds = new ArrayList<>();

    protected void trackSession(String sessionId) {
        if (sessionId != null && !sessionId.isBlank()) {
            createdSessionIds.add(sessionId);
        }
    }

    @AfterEach
    void cleanupCreatedSessions() {
        List<String> ids = new ArrayList<>(createdSessionIds);
        createdSessionIds.clear();
        List<String> failed = new ArrayList<>();
        for (String sessionId : ids) {
            try {
                ResponseEntity<String> response = restTemplate.exchange(
                        "/api/sessions/" + sessionId, HttpMethod.DELETE, null, String.class);
                int status = response.getStatusCode().value();
                if (status != 404 && (status < 200 || status >= 300)) {
                    failed.add(sessionId + " -> HTTP " + status);
                }
            } catch (Exception ex) {
                failed.add(sessionId + " -> " + ex.getClass().getSimpleName());
            }
        }
        if (!failed.isEmpty()) {
            throw new IllegalStateException(
                    "Session cleanup failed for: " + String.join(", ", failed));
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("opencode.base-url", OPENCODE_CONTAINER::getBaseUrl);
        registry.add("opencode.username", () -> "opencode");
        registry.add("opencode.password", () -> "opencode123");
    }

    private static boolean isCiEnvironment() {
        return System.getenv("CI") != null ||
               System.getenv("GITHUB_ACTIONS") != null ||
               System.getenv("GITLAB_CI") != null ||
               System.getenv("JENKINS_URL") != null;
    }
}
