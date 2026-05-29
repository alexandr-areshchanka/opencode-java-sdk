package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.SessionMessages200ResponseInner;
import opencode.sdk.model.SessionPrompt200Response;
import opencode.sdk.model.SessionPromptRequest;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/messages")
@RequiredArgsConstructor
@Slf4j
public class MessageController {

    private final OpenCodeService openCodeService;

    @GetMapping("/{sessionId}")
    public List<SessionMessages200ResponseInner> getSessionMessages(@PathVariable String sessionId) throws ApiException {
        return openCodeService.sessionApi().sessionMessages(sessionId, null, null, null, null);
    }

    @PostMapping("/{sessionId}/prompt")
    public SessionPrompt200Response sendPrompt(
            @PathVariable String sessionId,
            @RequestBody SessionPromptRequest request) throws ApiException {
        return openCodeService.sessionApi().sessionPrompt(sessionId, null, null, request);
    }

    @PostMapping("/{sessionId}/abort")
    public ResponseEntity<Void> abortMessage(@PathVariable String sessionId) throws ApiException {
        openCodeService.sessionApi().sessionAbort(sessionId, null, null);
        return ResponseEntity.noContent().build();
    }
}
