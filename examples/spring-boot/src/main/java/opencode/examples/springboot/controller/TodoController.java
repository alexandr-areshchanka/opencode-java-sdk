package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Todo;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/todos")
@RequiredArgsConstructor
public class TodoController {

    private final OpenCodeService openCodeService;

    @GetMapping("/{sessionId}")
    public List<Todo> getSessionTodos(@PathVariable String sessionId) throws ApiException {
        return openCodeService.sessionApi().sessionTodo(sessionId, null, null);
    }
}
