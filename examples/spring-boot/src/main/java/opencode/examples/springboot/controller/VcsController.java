package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.VcsInfo;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/vcs")
@RequiredArgsConstructor
public class VcsController {

    private final OpenCodeService openCodeService;

    @GetMapping
    public VcsInfo getVcsInfo() throws ApiException {
        return openCodeService.instanceApi().vcsGet(null, null);
    }
}
