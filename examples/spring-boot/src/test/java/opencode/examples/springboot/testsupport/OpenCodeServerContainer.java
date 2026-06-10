package opencode.examples.springboot.testsupport;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.testcontainers.containers.GenericContainer;
import org.testcontainers.containers.output.Slf4jLogConsumer;
import org.testcontainers.containers.wait.strategy.Wait;
import org.testcontainers.utility.DockerImageName;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.time.Duration;

public class OpenCodeServerContainer extends GenericContainer<OpenCodeServerContainer> {

    private static final Logger LOGGER = LoggerFactory.getLogger(OpenCodeServerContainer.class);

    private static final int OPENCODE_PORT = 4096;
    private static final String DEFAULT_USERNAME = "opencode";
    private static final String DEFAULT_PASSWORD = "opencode123";
    private static final String OPENCODE_VERSION = System.getProperty("opencode.version", "1.17.1");
    private static final String IMAGE_NAME = "opencode-server:" + OPENCODE_VERSION;

    public OpenCodeServerContainer() {
        super(validateImage());

        LOGGER.info("Initializing OpenCodeServerContainer...");

        withExposedPorts(OPENCODE_PORT);

        // Required environment variables for the OpenCode server
        String apiKey = System.getenv().getOrDefault("Z_AI_API_KEY", "a039df3a972e4c91a2a19096832d7bed.RDyMzFm7qZ4jKXvh");
        LOGGER.info("Using Z_AI_API_KEY: {}...", apiKey.substring(0, Math.min(10, apiKey.length())));

        withEnv("Z_AI_API_KEY", apiKey);
        withEnv("OPENCODE_SERVER_USERNAME", DEFAULT_USERNAME);
        withEnv("OPENCODE_SERVER_PASSWORD", DEFAULT_PASSWORD);
        withEnv("OPENCODE_SERVER_PORT", String.valueOf(OPENCODE_PORT));
        withEnv("OPENCODE_SERVER_HOST", "0.0.0.0");

        // Enable container log output for debugging
        withLogConsumer(new Slf4jLogConsumer(LOGGER));

        // Reduced timeout since we're using pre-built image
        // Health endpoint requires basic auth
        waitingFor(Wait.forHttp("/global/health")
                .forPort(OPENCODE_PORT)
                .withBasicCredentials(DEFAULT_USERNAME, DEFAULT_PASSWORD)
                .forStatusCode(200)
                .withStartupTimeout(Duration.ofSeconds(30)));

        // Add reusable container labels
        withLabel("reuse-hash", generateReuseHash());

        LOGGER.info("OpenCodeServerContainer configured with port {} and 30-second timeout", OPENCODE_PORT);
    }

    private static DockerImageName validateImage() {
        DockerImageName imageName = DockerImageName.parse(IMAGE_NAME);

        // Check if Docker is running and image exists by attempting to list the image
        if (!isDockerAvailable()) {
            throw new RuntimeException("Docker daemon is not running. Please start Docker and try again.");
        }

        if (!doesImageExist(IMAGE_NAME)) {
            throw new RuntimeException(
                "Docker image '" + IMAGE_NAME + "' not found. Build it using: " +
                "mvn clean install -Pbuild-docker-image or run ./build-docker-image.sh"
            );
        }

        return imageName;
    }

    private static boolean isDockerAvailable() {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "version", "--format", "{{.Server.Version}}");
            pb.redirectErrorStream(true);
            Process process = pb.start();
            boolean finished = process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            return finished && process.exitValue() == 0;
        } catch (Exception e) {
            return false;
        }
    }

    private static boolean doesImageExist(String imageName) {
        try {
            ProcessBuilder pb = new ProcessBuilder("docker", "images", "--format", "{{.Repository}}:{{.Tag}}", imageName);
            pb.redirectErrorStream(true);
            Process process = pb.start();

            try (BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    if (line.equals(imageName)) {
                        return true;
                    }
                }
            }

            process.waitFor(5, java.util.concurrent.TimeUnit.SECONDS);
            return false;
        } catch (Exception e) {
            LOGGER.warn("Failed to check if image exists: {}", e.getMessage());
            // Assume image exists to let Testcontainers handle the error
            return true;
        }
    }

    private String generateReuseHash() {
        // Generate a consistent hash based on image name and configuration
        String config = IMAGE_NAME + ":" + OPENCODE_PORT + ":" + DEFAULT_USERNAME;
        return String.valueOf(config.hashCode());
    }

    @Override
    public OpenCodeServerContainer withReuse(boolean reuse) {
        LOGGER.info("Setting container reuse to: {}", reuse);
        super.withReuse(reuse);
        return self();
    }

    public String getBaseUrl() {
        String baseUrl = String.format("http://%s:%d", getHost(), getMappedPort(OPENCODE_PORT));
        LOGGER.info("OpenCode server base URL: {}", baseUrl);
        return baseUrl;
    }

    public boolean isHealthy() {
        boolean running = isRunning();
        LOGGER.info("Container health check - isRunning: {}", running);
        return running;
    }

    @Override
    public void start() {
        LOGGER.info("Starting OpenCodeServerContainer...");
        LOGGER.info("Using pre-built Docker image: {}", IMAGE_NAME);
        try {
            super.start();
            LOGGER.info("OpenCodeServerContainer started successfully on port: {}", getMappedPort(OPENCODE_PORT));
        } catch (Exception e) {
            LOGGER.error("Failed to start OpenCodeServerContainer: {}", e.getMessage(), e);
            throw e;
        }
    }

    @Override
    public void stop() {
        LOGGER.info("Stopping OpenCodeServerContainer...");
        super.stop();
        LOGGER.info("OpenCodeServerContainer stopped");
    }
}
