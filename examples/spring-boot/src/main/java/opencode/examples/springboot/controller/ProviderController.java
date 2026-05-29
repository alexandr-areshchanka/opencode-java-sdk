package opencode.examples.springboot.controller;

import lombok.RequiredArgsConstructor;
import opencode.sdk.invoker.ApiException;
import opencode.sdk.model.ProviderAuthAuthorization;
import opencode.sdk.model.ProviderList200Response;
import opencode.sdk.model.ProviderOauthAuthorizeRequest;
import opencode.sdk.model.ProviderOauthCallbackRequest;
import opencode.sdk.springboot.OpenCodeService;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.Map;

@RestController
@RequestMapping("/api/providers")
@RequiredArgsConstructor
public class ProviderController {

    private final OpenCodeService openCodeService;

    @GetMapping
    public ProviderList200Response listProviders() throws ApiException {
        return openCodeService.providerApi().providerList(null, null);
    }

    @GetMapping("/{provider}")
    public Map<String, Object> getProvider(@PathVariable String provider) throws ApiException {
        ProviderList200Response response = openCodeService.providerApi().providerList(null, null);
        return response.getAll().stream()
                .filter(p -> provider.equals(p.getId()))
                .findFirst()
                .map(p -> Map.<String, Object>of("provider", p))
                .orElse(Map.of());
    }

    @PostMapping("/{provider}/oauth/authorize")
    public ProviderAuthAuthorization oauthAuthorize(
            @PathVariable String provider,
            @RequestBody(required = false) ProviderOauthAuthorizeRequest request) throws ApiException {
        return openCodeService.providerApi().providerOauthAuthorize(provider, null, null, request);
    }

    @GetMapping("/{provider}/oauth/callback")
    public ResponseEntity<?> oauthCallback(
            @PathVariable String provider,
            ProviderOauthCallbackRequest request) {
        try {
            Boolean result = openCodeService.providerApi().providerOauthCallback(provider, null, null, request);
            return ResponseEntity.ok(result);
        } catch (ApiException e) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body(Map.of("error", "OAuth callback failed", "message", e.getMessage()));
        } catch (Exception e) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body(Map.of("error", "Unexpected error", "message", e.getMessage()));
        }
    }
}
