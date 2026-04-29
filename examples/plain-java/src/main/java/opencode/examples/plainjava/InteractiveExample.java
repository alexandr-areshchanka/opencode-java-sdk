package opencode.examples.plainjava;

import opencode.examples.plainjava.testing.ExampleContext;
import opencode.examples.plainjava.testing.ResponseValidator;
import opencode.sdk.api.DefaultApi;
import opencode.sdk.invoker.ApiClient;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Base64;
import java.util.List;

public class InteractiveExample {

    private static final Logger logger = LoggerFactory.getLogger(InteractiveExample.class);

    private final DefaultApi api;
    private final ResponseValidator validator;

    public InteractiveExample(DefaultApi api) {
        this.api = api;
        this.validator = null;
    }

    public InteractiveExample(ExampleContext context) {
        this.api = context.getDefaultApi();
        this.validator = context.getValidator();
    }

    public void demonstrateInteractiveApis() {
        logger.info("--- Interactive APIs Example ---");

        demonstrateToolList();
        demonstrateQuestionApis();
        demonstratePermissionApis();
    }

    private void demonstrateToolList() {
        logger.info("\n=== Tool List ===");

        try {
            // First get available tool IDs
            List<String> toolIds = api.toolIds(null, null);
            logger.info("Available tool IDs count: {}", toolIds.size());

            if (!toolIds.isEmpty()) {
                logger.info("Sample tool IDs (first 5):");
                toolIds.stream().limit(5).forEach(id -> logger.info("  - {}", id));
            }

            // Get detailed tool list for a specific provider/model
            // Note: toolList requires provider and model parameters
            // Using common defaults - adjust based on your server configuration
            try {
                List<ToolListItem> tools = api.toolList("zai", "glm-4.7", null, null);
                logger.info("\nDetailed tool list (provider=zai, model=glm-4.7):");
                logger.info("  Tools count: {}", tools.size());

                if (!tools.isEmpty()) {
                    logger.info("  Sample tools (first 3):");
                    tools.stream().limit(3).forEach(tool -> {
                        logger.info("    - ID: {}", tool.getId());
                        logger.info("      Description: {}", tool.getDescription());
                    });
                }
            } catch (ApiException e) {
                logger.warn("Could not fetch detailed tool list (provider/model may not be available): {} - {}",
                        e.getCode(), e.getMessage());
            }

        } catch (ApiException e) {
            logger.error("Error listing tools: {} - {}", e.getCode(), e.getMessage());
        }
    }

    private void demonstrateQuestionApis() {
        logger.info("\n=== Question APIs ===");

        try {
            // List pending questions
            List<QuestionRequest> questions = api.questionList(null, null);
            logger.info("Pending questions count: {}", questions.size());

            if (!questions.isEmpty()) {
                logger.info("Current pending questions:");
                for (QuestionRequest question : questions) {
                    logger.info("  - Question ID: {}", question.getId());
                    logger.info("    Session ID: {}", question.getSessionID());
                    if (question.getQuestions() != null && !question.getQuestions().isEmpty()) {
                        logger.info("    Questions count: {}", question.getQuestions().size());
                    }
                    if (question.getTool() != null) {
                        logger.info("    Tool - MessageID: {}, CallID: {}",
                                question.getTool().getMessageID(),
                                question.getTool().getCallID());
                    }
                }

                // Demonstrate replying to the first question
                QuestionRequest firstQuestion = questions.get(0);
                demonstrateQuestionReply(firstQuestion.getId());
            } else {
                logger.info("No pending questions to reply to.");
                logger.info("Note: Questions are typically created during active AI sessions.");
            }

        } catch (ApiException e) {
            logger.error("Error with question APIs: {} - {}", e.getCode(), e.getMessage());
        }
    }

    private void demonstrateQuestionReply(String questionId) {
        logger.info("\n--- Replying to Question ---");
        logger.info("Question ID: {}", questionId);

        try {
            // Create a reply request with sample answers
            // The answers are a list of list of strings (selected labels for each question)
            QuestionReplyRequest replyRequest = new QuestionReplyRequest();
            List<List<String>> answers = new ArrayList<>();
            List<String> sampleAnswer = new ArrayList<>();
            sampleAnswer.add("sample_response");
            answers.add(sampleAnswer);
            replyRequest.setAnswers(answers);

            boolean success = api.questionReply(questionId, null, null, replyRequest);
            logger.info("Question reply successful: {}", success);

        } catch (ApiException e) {
            logger.error("Error replying to question: {} - {}", e.getCode(), e.getMessage());
        }
    }

    private void demonstrateQuestionReject(String questionId) {
        logger.info("\n--- Rejecting Question ---");
        logger.info("Question ID: {}", questionId);

        try {
            boolean success = api.questionReject(questionId, null, null);
            logger.info("Question rejection successful: {}", success);

        } catch (ApiException e) {
            logger.error("Error rejecting question: {} - {}", e.getCode(), e.getMessage());
        }
    }

    private void demonstratePermissionApis() {
        logger.info("\n=== Permission APIs ===");

        try {
            // List pending permission requests
            List<PermissionRequest> permissions = api.permissionList(null, null);
            logger.info("Pending permission requests count: {}", permissions.size());

            if (!permissions.isEmpty()) {
                logger.info("Current pending permissions:");
                for (PermissionRequest permission : permissions) {
                    logger.info("  - Permission ID: {}", permission.getId());
                    logger.info("    Session ID: {}", permission.getSessionID());
                    logger.info("    Permission: {}", permission.getPermission());
                    if (permission.getPatterns() != null && !permission.getPatterns().isEmpty()) {
                        logger.info("    Patterns: {}", permission.getPatterns());
                    }
                    if (permission.getTool() != null) {
                        logger.info("    Tool - MessageID: {}, CallID: {}",
                                permission.getTool().getMessageID(),
                                permission.getTool().getCallID());
                    }
                }

                // Demonstrate replying to the first permission
                PermissionRequest firstPermission = permissions.get(0);
                demonstratePermissionReply(firstPermission.getId());
            } else {
                logger.info("No pending permission requests.");
                logger.info("Note: Permission requests are typically created during active AI sessions.");
            }

        } catch (ApiException e) {
            logger.error("Error with permission APIs: {} - {}", e.getCode(), e.getMessage());
        }
    }

    private void demonstratePermissionReply(String permissionId) {
        logger.info("\n--- Replying to Permission ---");
        logger.info("Permission ID: {}", permissionId);

        try {
            // Create a permission reply request - allow the action once
            PermissionReplyRequest replyRequest = new PermissionReplyRequest();
            replyRequest.setReply(PermissionReplyRequest.ReplyEnum.ONCE);
            replyRequest.setMessage("Approved by Java SDK example");

            boolean success = api.permissionReply(permissionId, null, null, replyRequest);
            logger.info("Permission reply successful: {}", success);
            logger.info("Action reply: ONCE (allowed once)");

        } catch (ApiException e) {
            logger.error("Error replying to permission: {} - {}", e.getCode(), e.getMessage());
        }
    }

    public static void main(String[] args) {
        logger.info("Starting Interactive Example");
        logger.info("============================");

        ApiClient apiClient = new ApiClient();
        apiClient.updateBaseUri("http://localhost:4096");
        String credentials = "opencode:opencode123";
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        apiClient.setRequestInterceptor(builder -> builder.header("Authorization", "Basic " + encoded));
        DefaultApi api = new DefaultApi(apiClient);

        try {
            InteractiveExample example = new InteractiveExample(api);
            example.demonstrateInteractiveApis();

            logger.info("\n============================");
            logger.info("Interactive Example completed!");

        } catch (Exception e) {
            System.err.println("Fatal error running Interactive Example: " + e.getMessage());
            e.printStackTrace();
            System.exit(1);
        }
    }
}
