package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.EventApi;
import opencode.sdk.api.GlobalApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Event;
import opencode.sdk.model.GlobalEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.time.Duration;
import java.util.Base64;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.function.Consumer;

public class EventStreamingExample {

    private static final Logger logger = LoggerFactory.getLogger(EventStreamingExample.class);

    private final EventApi eventApi;
    private final GlobalApi globalApi;
    private final ResponseValidator validator;
    private final ApiClient apiClient;

    private static final long SSE_COLLECTION_DURATION_SECONDS = 5;

    public EventStreamingExample(ApiClient apiClient) {
        this.apiClient = apiClient;
        this.eventApi = new EventApi(apiClient);
        this.globalApi = new GlobalApi(apiClient);
        this.validator = null;
    }

    public EventStreamingExample(ExampleContext context) {
        this.apiClient = context.getApiClient();
        this.eventApi = new EventApi(context.getApiClient());
        this.globalApi = new GlobalApi(context.getApiClient());
        this.validator = context.getValidator();
    }

    public void demonstrateEventStreaming() {
        try {
            logger.info("=== Event Streaming Example ===");

            // Skip SSE streaming in automated testing (long-running/blocking operations)
            if (validator != null) {
                demonstrateAsyncEventStreaming();
            } else {
                // Demonstrate project event subscription
                demonstrateProjectEventSubscribe();

                // Demonstrate global event subscription
                demonstrateGlobalEvent();
            }

            logger.info("=== Event Streaming Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during event streaming operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during event streaming operations: {}", e.getMessage(), e);
        }
    }

    private void demonstrateAsyncEventStreaming() {
        logger.info("Running async SSE event streaming in testing mode");

        String sseUrl = apiClient.getBaseUri() + "/global/event";

        HttpClient httpClient = HttpClient.newBuilder()
                .connectTimeout(Duration.ofSeconds(10))
                .build();

        HttpRequest.Builder requestBuilder = HttpRequest.newBuilder()
                .uri(URI.create(sseUrl))
                .header("Accept", "text/event-stream")
                .GET();

        Consumer<HttpRequest.Builder> interceptor = apiClient.getRequestInterceptor();
        if (interceptor != null) {
            interceptor.accept(requestBuilder);
        }

        HttpRequest request = requestBuilder.build();

        CountDownLatch connectedLatch = new CountDownLatch(1);
        CountDownLatch firstEventLatch = new CountDownLatch(1);
        AtomicInteger eventCount = new AtomicInteger(0);
        AtomicBoolean running = new AtomicBoolean(true);
        final InputStream[] streamHolder = new InputStream[1];

        Thread sseThread = new Thread(() -> {
            try {
                HttpResponse<InputStream> response = httpClient.send(
                        request, HttpResponse.BodyHandlers.ofInputStream());
                streamHolder[0] = response.body();
                connectedLatch.countDown();

                BufferedReader reader = new BufferedReader(
                        new InputStreamReader(streamHolder[0], StandardCharsets.UTF_8));
                String line;
                while (running.get() && (line = reader.readLine()) != null) {
                    if (line.startsWith("data:")) {
                        int count = eventCount.incrementAndGet();
                        String data = line.substring(5).trim();
                        logger.info("SSE event #{}: {}", count, data);
                        firstEventLatch.countDown();
                    }
                }
            } catch (IOException e) {
                if (running.get()) {
                    logger.warn("SSE stream I/O error: {}", e.getMessage());
                }
            } catch (Exception e) {
                if (running.get()) {
                    logger.warn("SSE stream error: {}", e.getMessage());
                }
            }
        }, "sse-event-reader");
        sseThread.setDaemon(true);
        sseThread.start();

        try {
            boolean connected = connectedLatch.await(10, TimeUnit.SECONDS);
            if (!connected) {
                logger.error("Failed to connect to SSE endpoint within timeout");
                running.set(false);
                sseThread.interrupt();
                return;
            }
            logger.info("Connected to SSE endpoint: {}", sseUrl);

            boolean receivedFirst = firstEventLatch.await(
                    SSE_COLLECTION_DURATION_SECONDS, TimeUnit.SECONDS);

            running.set(false);

            InputStream is = streamHolder[0];
            if (is != null) {
                try {
                    is.close();
                } catch (IOException ignored) {
                }
            }

            sseThread.join(1000);

            int totalEvents = eventCount.get();
            logger.info("SSE event collection completed: {} events received in {} seconds",
                    totalEvents, SSE_COLLECTION_DURATION_SECONDS);

            if (receivedFirst) {
                validator.validateNonNull("SSE events received", "SSE event stream");
            } else {
                logger.warn("No SSE events received within {} second timeout - "
                        + "server may not be generating events", SSE_COLLECTION_DURATION_SECONDS);
                validator.validateNonNull(null, "SSE event stream");
            }
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            running.set(false);
            InputStream is = streamHolder[0];
            if (is != null) {
                try { is.close(); } catch (IOException ignored) {}
            }
            sseThread.interrupt();
            logger.warn("SSE collection interrupted");
        }
    }

    private void demonstrateProjectEventSubscribe() throws ApiException {
        logger.info("\n--- Subscribing to Project Events (SSE) ---");

        Event event = eventApi.eventSubscribe(
                null,  // directory - uses current directory
                null   // workspace - uses default workspace
        );

        if (validator != null) {
            validator.validateNonNull(event, "event");
        }

        logger.info("Successfully subscribed to project events!");
        logEventDetails(event);
    }

    private void demonstrateGlobalEvent() throws ApiException {
        logger.info("\n--- Subscribing to Global Events (SSE) ---");

        GlobalEvent globalEvent = globalApi.globalEvent();

        if (validator != null) {
            validator.validateNonNull(globalEvent, "global event");
        }

        logger.info("Successfully subscribed to global events!");
        logger.info("  Directory: {}", globalEvent.getDirectory());

        if (globalEvent.getPayload() != null) {
            logger.info("  Event Payload:");
            logger.info("    {}", globalEvent.getPayload());
        }
    }

    private void logEventDetails(Event event) {
        if (event == null) {
            logger.info("    Event: null");
            return;
        }

        logger.info("    Event Type: {}", event.getClass().getSimpleName());
        logger.info("    Event toString: {}", event.toString());
    }

    public static void main(String[] args) {
        logger.info("Starting Event Streaming Example");
        logger.info("=================================");

        // Configure the client with Basic Auth
        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));

        try {
            // Run the example
            EventStreamingExample example = new EventStreamingExample(apiClient);
            example.demonstrateEventStreaming();

            logger.info("\n");
            logger.info("=================================");
            logger.info("Example completed successfully!");

        } catch (Exception e) {
            logger.error("Error running example: {}", e.getMessage(), e);
            System.err.println("Fatal error: " + e.getMessage());
            System.exit(1);
        }
    }
}
