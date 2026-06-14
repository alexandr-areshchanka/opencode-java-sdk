package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/instances")
@RequiredArgsConstructor
public class InstanceController {

    private final OpenCodeService openCodeService;

    @GetMapping
    public ResponseEntity<String> listInstances() {
        return ResponseEntity.status(HttpStatus.NOT_IMPLEMENTED).body("Listing instances is not supported by the API");
    }

    @PostMapping
    public ResponseEntity<String> createInstance() {
        return ResponseEntity.status(501).body("Instance creation not supported by API");
    }

    @DeleteMapping
    public Boolean disposeInstance(@RequestParam(required = false) String directory,
                                   @RequestParam(required = false) String workspace) throws ApiException {
        return openCodeService.instanceApi().instanceDispose(directory, workspace);
    }
}
