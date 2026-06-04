package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/sessions")
@RequiredArgsConstructor
public class SessionCrudController {

    private final OpenCodeService openCodeService;

    private boolean isValidSessionId(String sessionId) {
        return sessionId != null && sessionId.startsWith("ses_");
    }

    @GetMapping
    public List<Session> listSessions() throws ApiException {
        return openCodeService.sessionApi().sessionList(null, null, null, null, new ExperimentalSessionListRootsParameter(), null, null, null);
    }

    @PostMapping
    public Session createSession(@RequestBody SessionCreateRequest request) throws ApiException {
        return openCodeService.sessionApi().sessionCreate(null, null, request);
    }

    @GetMapping("/{sessionId}")
    public ResponseEntity<?> getSession(@PathVariable String sessionId) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        Session session = openCodeService.sessionApi().sessionGet(sessionId, null, null);
        return ResponseEntity.ok(session);
    }

    @PatchMapping("/{sessionId}")
    public ResponseEntity<?> updateSession(
            @PathVariable String sessionId,
            @RequestBody SessionUpdateRequest request) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        openCodeService.sessionApi().sessionUpdate(sessionId, null, null, request);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{sessionId}")
    public ResponseEntity<?> deleteSession(@PathVariable String sessionId) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        openCodeService.sessionApi().sessionDelete(sessionId, null, null);
        return ResponseEntity.noContent().build();
    }

    @PostMapping("/{sessionId}/init")
    public ResponseEntity<?> initSession(
            @PathVariable String sessionId,
            @RequestBody SessionInitRequest request) throws ApiException {
        if (!isValidSessionId(sessionId)) {
            return ResponseEntity.notFound().build();
        }
        openCodeService.sessionApi().sessionInit(sessionId, null, null, request);
        return ResponseEntity.noContent().build();
    }
}
