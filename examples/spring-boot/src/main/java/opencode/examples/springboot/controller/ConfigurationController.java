package opencode.examples.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Config;
import opencode.sdk.model.ConfigProviders200Response;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/config")
@RequiredArgsConstructor
public class ConfigurationController {

    private final OpenCodeService openCodeService;

    @GetMapping("/project")
    public Config getProjectConfig() throws ApiException {
        return openCodeService.configApi().configGet(null, null);
    }

    @GetMapping("/global")
    public Config getGlobalConfig() throws ApiException {
        return openCodeService.globalApi().globalConfigGet();
    }

    @PatchMapping("/project")
    public ResponseEntity<?> updateConfig(@Valid @RequestBody Config config) throws ApiException {
        try {
            Config result = openCodeService.configApi().configUpdate(null, null, config);
            return ResponseEntity.ok(result);
        } catch (ApiException e) {
            if (e.getCode() == 400) {
                return ResponseEntity.badRequest()
                        .body("Invalid configuration: " + e.getMessage());
            }
            throw e;
        }
    }

    @GetMapping("/providers")
    public ConfigProviders200Response getProviders() throws ApiException {
        return openCodeService.configApi().configProviders(null, null);
    }
}
