package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions/advanced")
@RequiredArgsConstructor
public class SessionAdvancedController {

    private final OpenCodeService openCodeService;

    private boolean isValidSessionId(String sessionId) {
        return sessionId != null && sessionId.startsWith("ses_");
    }

    @PostMapping("/{sessionId}/fork")
    public ResponseEntity<?> forkSession(
            @PathVariable String sessionId,
            @RequestBody SessionForkRequest request) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        Session session = openCodeService.sessionApi().sessionFork(sessionId, null, null, request);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{sessionId}/revert")
    public ResponseEntity<?> revertSession(
            @PathVariable String sessionId,
            @RequestBody SessionRevertRequest request) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        Session session = openCodeService.sessionApi().sessionRevert(sessionId, null, null, request);
        return ResponseEntity.ok(session);
    }

    @GetMapping("/{sessionId}/share")
    public ResponseEntity<?> getShareInfo(@PathVariable String sessionId) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        Session session = openCodeService.sessionApi().sessionShare(sessionId, null, null);
        return ResponseEntity.ok(session);
    }

    @PostMapping("/{sessionId}/summarize")
    public ResponseEntity<?> summarizeSession(
            @PathVariable String sessionId,
            @RequestBody SessionSummarizeRequest request) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        Boolean result = openCodeService.sessionApi().sessionSummarize(sessionId, null, null, request);
        return ResponseEntity.ok(result);
    }

    @GetMapping("/{sessionId}/children")
    public ResponseEntity<?> getChildren(@PathVariable String sessionId) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        List<Session> sessions = openCodeService.sessionApi().sessionChildren(sessionId, null, null);
        return ResponseEntity.ok(sessions);
    }

    @PostMapping("/{sessionId}/command")
    public ResponseEntity<?> sendCommand(
            @PathVariable String sessionId,
            @RequestBody SessionCommandRequest request) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        SessionPrompt200Response response = openCodeService.sessionApi().sessionCommand(sessionId, null, null, request);
        return ResponseEntity.ok(response);
    }

    @PostMapping("/{sessionId}/shell")
    public ResponseEntity<?> executeShell(
            @PathVariable String sessionId,
            @RequestBody SessionShellRequest request) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        SessionShell200Response message = openCodeService.sessionApi().sessionShell(sessionId, null, null, request);
        return ResponseEntity.ok(message);
    }
}
