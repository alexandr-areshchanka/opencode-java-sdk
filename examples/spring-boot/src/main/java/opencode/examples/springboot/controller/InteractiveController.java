package opencode.examples.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/interactive")
@RequiredArgsConstructor
public class InteractiveController {

    private final OpenCodeService openCodeService;

    @GetMapping("/questions")
    public List<QuestionRequest> listQuestions() throws ApiException {
        return openCodeService.questionApi().questionList(null, null);
    }

    @PostMapping("/questions/reply")
    public ResponseEntity<?> replyToQuestion(
            @RequestParam String requestId,
            @Valid @RequestBody QuestionReplyRequest request) throws ApiException {
        if (requestId == null || requestId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request ID", "message", "Request ID cannot be null or empty"));
        }
        return ResponseEntity.ok(openCodeService.questionApi().questionReply(requestId, null, null, request));
    }

    @GetMapping("/permissions")
    public List<PermissionRequest> listPermissions() throws ApiException {
        return openCodeService.permissionApi().permissionList(null, null);
    }

    @PostMapping("/permissions/reply")
    public ResponseEntity<?> replyToPermission(
            @RequestParam String requestId,
            @Valid @RequestBody PermissionReplyRequest request) throws ApiException {
        if (requestId == null || requestId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid request ID", "message", "Request ID cannot be null or empty"));
        }
        return ResponseEntity.ok(openCodeService.permissionApi().permissionReply(requestId, null, null, request));
    }

    @PostMapping("/permissions/respond")
    public ResponseEntity<?> respondToPermission(
            @RequestParam String sessionId,
            @RequestParam String permissionId,
            @Valid @RequestBody PermissionRespondRequest request) throws ApiException {
        if (sessionId == null || sessionId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid session ID", "message", "Session ID cannot be null or empty"));
        }
        if (permissionId == null || permissionId.trim().isEmpty()) {
            return ResponseEntity.badRequest()
                    .body(Map.of("error", "Invalid permission ID", "message", "Permission ID cannot be null or empty"));
        }
        return ResponseEntity.ok(openCodeService.sessionApi().permissionRespond(sessionId, permissionId, null, null, request));
    }
}
