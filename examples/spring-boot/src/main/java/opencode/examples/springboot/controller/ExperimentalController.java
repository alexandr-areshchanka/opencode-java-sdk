package opencode.examples.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/experimental")
@RequiredArgsConstructor
public class ExperimentalController {

    private final OpenCodeService openCodeService;

    @PostMapping("/workspace")
    public Workspace createWorkspace(@Valid @RequestBody ExperimentalWorkspaceCreateRequest request) throws ApiException {
        return openCodeService.workspaceApi().experimentalWorkspaceCreate(null, null, request);
    }

    @GetMapping("/worktree")
    public List<String> listWorktrees() throws ApiException {
        return openCodeService.experimentalApi().worktreeList(null, null);
    }

    @PostMapping("/worktree")
    public Worktree createWorktree(@Valid @RequestBody WorktreeCreateInput input) throws ApiException {
        return openCodeService.experimentalApi().worktreeCreate(null, null, input);
    }

    @DeleteMapping("/worktree")
    public Boolean removeWorktree(@Valid @RequestBody WorktreeRemoveInput input) throws ApiException {
        return openCodeService.experimentalApi().worktreeRemove(null, null, input);
    }
}
