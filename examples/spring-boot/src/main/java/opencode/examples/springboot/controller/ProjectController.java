package opencode.examples.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import opencode.sdk.model.Project;
import opencode.sdk.model.ProjectUpdateRequest;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/projects")
@RequiredArgsConstructor
public class ProjectController {

    private final OpenCodeService openCodeService;

    @GetMapping
    public List<Project> listProjects() throws Exception {
        return openCodeService.projectApi().projectList(null, null);
    }

    @GetMapping("/current")
    public Project getCurrentProject() throws Exception {
        return openCodeService.projectApi().projectCurrent(null, null);
    }

    @PatchMapping("/current")
    public Project updateCurrentProject(@Valid @RequestBody ProjectUpdateRequest request) throws Exception {
        Project currentProject = openCodeService.projectApi().projectCurrent(null, null);
        return openCodeService.projectApi().projectUpdate(currentProject.getId(), null, null, request);
    }
}
