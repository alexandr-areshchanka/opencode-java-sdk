package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.SessionApi;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Session;
import opencode.sdk.model.Todo;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.List;

public class TodoExample {

    private static final Logger logger = LoggerFactory.getLogger(TodoExample.class);

    private final SessionApi sessionApi;
    private final ResponseValidator validator;

    public TodoExample(ExampleContext context) {
        this.sessionApi = new SessionApi(context.getApiClient());
        this.validator = context.getValidator();
    }

    public void demonstrateTodoOperations() {
        try {
            logger.info("=== Todo Example ===");

            // First, we need a session to get todos from
            // List available sessions to find one with potential todos
            List<Session> sessions = sessionApi.sessionList(
                    null,  // directory
                    null,  // workspace
                    null,  // scope
                    null,  // path
                    null,  // roots
                    null,  // start
                    null,  // search
                    null   // limit
            );

            if (sessions.isEmpty()) {
                logger.info("No sessions found. Creating a new session to demonstrate todo listing...");
                // Note: Todos are typically populated through AI interactions
                // For this example, we'll show the API usage even if no todos exist
            } else {
                // Use the first session to demonstrate todo listing
                String sessionId = sessions.get(0).getId();
                logger.info("Using session ID: {}", sessionId);

                // List todos for the session
                listTodos(sessionId);
            }

            logger.info("=== Todo Example Completed Successfully ===");

        } catch (ApiException e) {
            logger.error("API Error during todo operations: {} - {}", e.getCode(), e.getMessage(), e);
        } catch (Exception e) {
            logger.error("Unexpected error during todo operations: {}", e.getMessage(), e);
        }
    }

    private void listTodos(String sessionId) throws ApiException {
        logger.info("\n--- Listing Todos for Session: {} ---", sessionId);

        List<Todo> todos = sessionApi.sessionTodo(
                sessionId,
                null,  // directory
                null   // workspace
        );

        if (validator != null) {
            validator.validateCollection(todos, "todos");
        }

        if (todos == null || todos.isEmpty()) {
            logger.info("No todos found for this session.");
            logger.info("Note: Todos are typically created during AI assistant interactions.");
        } else {
            logger.info("Found {} todos:", todos.size());
            for (Todo todo : todos) {
                if (validator != null) {
                    validator.validateNonNull(todo.getContent(), "todo content");
                }

                logger.info("  - Content: {}", todo.getContent());
                logger.info("    Status: {}", todo.getStatus());
                logger.info("    Priority: {}", todo.getPriority());
            }
        }
    }

}
