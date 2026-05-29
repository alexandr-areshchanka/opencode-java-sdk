package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.*;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/mcp")
@RequiredArgsConstructor
public class McpController {

    private final OpenCodeService openCodeService;

    @GetMapping
    public Map<String, MCPStatus> listMcpServers() throws ApiException {
        return openCodeService.mcpApi().mcpStatus(null, null);
    }

    @PostMapping
    public Map<String, MCPStatus> addMcpServer(@RequestBody McpAddRequest request) throws ApiException {
        return openCodeService.mcpApi().mcpAdd(null, null, request);
    }

    @GetMapping("/{name}/status")
    public MCPStatus getMcpServerStatus(@PathVariable String name) throws ApiException {
        Map<String, MCPStatus> statusMap = openCodeService.mcpApi().mcpStatus(null, null);
        return statusMap.get(name);
    }

    @PostMapping("/{name}/auth/start")
    public McpAuthStart200Response startMcpAuth(@PathVariable String name) throws ApiException {
        return openCodeService.mcpApi().mcpAuthStart(name, null, null);
    }

    @PostMapping("/{name}/auth/callback")
    public MCPStatus mcpAuthCallback(
            @PathVariable String name,
            @RequestBody McpAuthCallbackRequest request) throws ApiException {
        return openCodeService.mcpApi().mcpAuthCallback(name, null, null, request);
    }

    @DeleteMapping("/{name}/auth")
    public McpAuthRemove200Response removeMcpAuth(@PathVariable String name) throws ApiException {
        return openCodeService.mcpApi().mcpAuthRemove(name, null, null);
    }
}
