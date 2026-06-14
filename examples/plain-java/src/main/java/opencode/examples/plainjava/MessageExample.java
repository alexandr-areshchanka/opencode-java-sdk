package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResourceTracker;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.GlobalApi;
import opencode.sdk.api.SessionApi;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class MessageExample {

    private static final Logger logger = LoggerFactory.getLogger(MessageExample.class);

    private final SessionApi sessionApi;
    private final GlobalApi globalApi;
    private final ResponseValidator validator;
    private final ResourceTracker tracker;

    public MessageExample(ExampleContext context) {
        this.sessionApi = new SessionApi(context.getApiClient());
        this.globalApi = new GlobalApi(context.getApiClient());
        this.validator = context.getValidator();
        this.tracker = context.getResourceTracker();
    }

    public void demonstrateMessaging() {
        try {
            logger.info("=== Message Example ===");

            // First, create a session to work with
            String sessionId = createSession();

            // Send a prompt to the session
            sendMessage(sessionId, "What is Java?");

            // List messages in the session
            listMessages(sessionId);

            // Send another message
            sendMessage(sessionId, "What are the main features of Java 21?");

            // List messages again to see the conversation history
            listMessages(sessionId);

            logger.info("=== Message Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during messaging operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during messaging operations: {}", e.getMessage(), e);
        }
    }

    private String createSession() throws ApiException {
        logger.info("\n--- Creating Session for Messaging ---");

        SessionCreateRequest request = new SessionCreateRequest();
        request.setTitle("Java Q&A Session");

        Session session = sessionApi.sessionCreate(
                null,  // directory
                null,  // workspace
                request
        );

        if (validator != null) {
            validator.validateNonNull(session, "created session");
            validator.validateNonNull(session.getId(), "session id");
        }

        if (tracker != null) {
            tracker.trackSession(session.getId());
        }

        logger.info("Session created for messaging: {}", session.getId());
        return session.getId();
    }

    private void sendMessage(String sessionId, String userMessage) throws ApiException {
        logger.info("\n--- Sending Message ---");
        logger.info("User: {}", userMessage);

        // Build the prompt request with text content
        SessionPromptRequest request = buildPromptRequest(userMessage);

        // Send the prompt and get AI response
        SessionPrompt200Response response = sessionApi.sessionPrompt(
                sessionId,
                null,  // directory
                null,  // workspace
                request
        );

        if (validator != null) {
            validator.validateNonNull(response, "prompt response");
        }

        // Log the AI's response
        if (response.getParts() != null && !response.getParts().isEmpty()) {
            String responseText = extractTextFromParts(response.getParts());
            logger.info("AI: {}", responseText);
        }

        // Log metadata about the response
        if (response.getInfo() != null) {
            logger.debug("Response info: {}", response.getInfo());
        }
    }

    private void listMessages(String sessionId) throws ApiException {
        logger.info("\n--- Listing Messages in Session ---");

        List<SessionMessages200ResponseInner> messages = sessionApi.sessionMessages(
                sessionId,
                null,   // directory
                null,   // workspace
                20,     // limit - last 20 messages
                null    // before
        );

        if (validator != null) {
            validator.validateCollection(messages, "messages");
        }

        logger.info("Total messages in session: {}", messages.size());

        int messageNum = 1;
        for (SessionMessages200ResponseInner message : messages) {
            logger.info("Message {}:", messageNum++);

            Message info = message.getInfo();
            String role = info != null ? "user" : "unknown";
            String content = extractTextFromParts(message.getParts());

            logger.info("  Role: {}, Content: {}", role, content);
        }
    }

    private SessionPromptRequest buildPromptRequest(String userMessage) {
        SessionPromptRequest request = new SessionPromptRequest();

        // Create text part input
        TextPartInput textPart = new TextPartInput();
        textPart.setType(TextPartInput.TypeEnum.TEXT);
        textPart.setText(userMessage);

        // Create request part and add text part to it
        SessionPromptRequestPartsInner requestPart = new SessionPromptRequestPartsInner(textPart);

        // Add the part to the request
        request.addPartsItem(requestPart);

        return request;
    }

    private String extractTextFromParts(List<Part> parts) {
        if (parts == null || parts.isEmpty()) {
            return "[No content]";
        }

        StringBuilder text = new StringBuilder();
        for (Part part : parts) {
            Object actualInstance = part.getActualInstance();
            if (actualInstance instanceof TextPart textPart && textPart.getText() != null) {
                text.append(textPart.getText());
            }
        }
        return text.toString();
    }

}
