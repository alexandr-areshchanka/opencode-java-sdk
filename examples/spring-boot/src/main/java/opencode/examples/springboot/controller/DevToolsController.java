package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.FormatterStatus;
import opencode.sdk.model.LSPStatus;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/devtools")
@RequiredArgsConstructor
public class DevToolsController {

    private final OpenCodeService openCodeService;

    @GetMapping("/lsp")
    public List<LSPStatus> getLspStatus() throws ApiException {
        return openCodeService.instanceApi().lspStatus(null, null);
    }

    @GetMapping("/formatter")
    public List<FormatterStatus> getFormatterStatus() throws ApiException {
        return openCodeService.instanceApi().formatterStatus(null, null);
    }
}
