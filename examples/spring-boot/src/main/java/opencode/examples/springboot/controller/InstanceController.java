package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.GlobalHealth200Response;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instances")
@RequiredArgsConstructor
public class InstanceController {

    private final OpenCodeService openCodeService;

    @GetMapping
    public GlobalHealth200Response listInstances() throws ApiException {
        return openCodeService.globalApi().globalHealth();
    }

    @PostMapping
    public ResponseEntity<String> createInstance() {
        return ResponseEntity.status(501).body("Instance creation not supported by API");
    }

    @DeleteMapping("/{instanceId}")
    public Boolean disposeInstance(@PathVariable String instanceId) throws ApiException {
        return openCodeService.instanceApi().instanceDispose(null, null);
    }
}
