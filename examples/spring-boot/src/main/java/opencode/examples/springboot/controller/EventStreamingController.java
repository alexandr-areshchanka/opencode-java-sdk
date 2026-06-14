package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Event;
import opencode.sdk.model.GlobalEvent;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.http.MediaType;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/events")
@RequiredArgsConstructor
public class EventStreamingController {

    private final OpenCodeService openCodeService;

    @GetMapping(value = "/", produces = MediaType.APPLICATION_JSON_VALUE)
    public Event subscribeToEvents() throws ApiException {
        return openCodeService.eventApi().eventSubscribe(null, null);
    }

    @GetMapping(value = "/global", produces = MediaType.APPLICATION_JSON_VALUE)
    public GlobalEvent subscribeToGlobalEvents() throws ApiException {
        return openCodeService.globalApi().globalEvent();
    }
}
