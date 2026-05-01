package opencode.examples.springboot.testsupport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.resttestclient.TestRestTemplate;
import org.springframework.boot.resttestclient.autoconfigure.AutoConfigureTestRestTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

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
