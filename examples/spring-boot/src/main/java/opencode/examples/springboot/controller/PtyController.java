package opencode.examples.springboot.controller;

import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.Pty;
import opencode.sdk.model.PtyCreateRequest;
import opencode.sdk.model.PtyUpdateRequest;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/pty")
@RequiredArgsConstructor
public class PtyController {

    private final OpenCodeService openCodeService;

    @GetMapping
    public List<Pty> listPtys() throws ApiException {
        return openCodeService.ptyApi().ptyList(null, null);
    }

    @PostMapping
    public Pty createPty(@Valid @RequestBody PtyCreateRequest request) throws ApiException {
        return openCodeService.ptyApi().ptyCreate(null, null, request);
    }

    @GetMapping("/{id}")
    public Pty getPty(@PathVariable String id) throws ApiException {
        return openCodeService.ptyApi().ptyGet(id, null, null);
    }

    @PatchMapping("/{id}")
    public Pty updatePty(@PathVariable String id, @Valid @RequestBody PtyUpdateRequest request) throws ApiException {
        return openCodeService.ptyApi().ptyUpdate(id, null, null, request);
    }

    @DeleteMapping("/{id}")
    public Boolean deletePty(@PathVariable String id) throws ApiException {
        return openCodeService.ptyApi().ptyRemove(id, null, null);
    }
}
