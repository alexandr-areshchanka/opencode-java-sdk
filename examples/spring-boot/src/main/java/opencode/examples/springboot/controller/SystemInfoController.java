package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.AppSkills200ResponseInner;
import opencode.sdk.model.GlobalHealth200Response;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/system")
@RequiredArgsConstructor
public class SystemInfoController {

    private final OpenCodeService openCodeService;

    @GetMapping("/health")
    public GlobalHealth200Response getHealth() throws ApiException {
        return openCodeService.globalApi().globalHealth();
    }

    @GetMapping("/skills")
    public List<AppSkills200ResponseInner> getSkills() throws ApiException {
        return openCodeService.instanceApi().appSkills(null, null);
    }
}
