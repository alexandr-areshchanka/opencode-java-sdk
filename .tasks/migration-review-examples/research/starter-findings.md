# Spring Boot Starter Module - Current Code Analysis

## Module Overview

**Location**: `opencode-spring-boot-starter/`
**Package**: `opencode.sdk.springboot` / `opencode.sdk.springboot.autoconfigure`
**Artifact**: `opencode-spring-boot-starter`

---

## Dependencies (pom.xml)

```xml
<dependencies>
    <!-- OpenCode SDK -->
    <dependency>
        <groupId>io.opencode</groupId>
        <artifactId>opencode-sdk</artifactId>
        <version>${project.version}</version>
        <scope>compile</scope>
    </dependency>

    <!-- Spring Boot Starter WebMvc (renamed from spring-boot-starter-web in Spring Boot 4.0) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-webmvc</artifactId>
        <scope>compile</scope>
    </dependency>

    <!-- Jackson 2 compatibility module (Jackson 3 migration deferred) -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-jackson2</artifactId>
    </dependency>

    <!-- Spring Boot Configuration Processor -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-configuration-processor</artifactId>
        <scope>provided</scope>
    </dependency>
</dependencies>
```

---

## File 1: `OpenCodeProperties.java`

**Path**: `src/main/java/opencode/sdk/springboot/autoconfigure/OpenCodeProperties.java`
**Lines**: 36

### Complete Content:

```java
package opencode.sdk.springboot.autoconfigure;

import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "opencode")
public class OpenCodeProperties {

    private String baseUrl = "http://localhost:4096";
    private String username;
    private String password;

    public String getBaseUrl() {
        return baseUrl;
    }

    public void setBaseUrl(String baseUrl) {
        this.baseUrl = baseUrl;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }
}
```

### Analysis:
- **Imports**: Only `org.springframework.boot.context.properties.ConfigurationProperties` — no SDK imports
- **Fields**:
  - `baseUrl` (String, default `"http://localhost:4096"`)
  - `username` (String, default null)
  - `password` (String, default null)
- **Methods**: Simple getters/setters for all 3 fields
- **No broken references**: This file has no dependency on API classes
- **Config prefix**: `opencode` (properties: `opencode.base-url`, `opencode.username`, `opencode.password`)

---

## File 2: `OpenCodeAutoConfiguration.java`

**Path**: `src/main/java/opencode/sdk/springboot/autoconfigure/OpenCodeAutoConfiguration.java`
**Lines**: 53

### Complete Content:

```java
package opencode.sdk.springboot.autoconfigure;

import opencode.sdk.api.DefaultApi;                          // ❌ BROKEN - DefaultApi no longer exists
import opencode.sdk.invoker.ApiClient;                       // ✅ OK
import org.springframework.boot.autoconfigure.condition.ConditionalOnClass;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.Base64;

@Configuration
@ConditionalOnClass(ApiClient.class)
@EnableConfigurationProperties(OpenCodeProperties.class)
public class OpenCodeAutoConfiguration {

    @Bean
    @ConditionalOnMissingBean
    public ApiClient apiClient(OpenCodeProperties properties) {
        ApiClient client = new ApiClient();

        if (properties.getBaseUrl() != null) {
            client.updateBaseUri(properties.getBaseUrl());
        }

        if (properties.getUsername() != null && properties.getPassword() != null) {
            String authHeader = createBasicAuthHeader(properties.getUsername(), properties.getPassword());
            client.setRequestInterceptor(builder -> builder.header("Authorization", authHeader));
        }

        return client;
    }

    @Bean
    @ConditionalOnMissingBean
    public DefaultApi defaultApi(ApiClient apiClient) {      // ❌ BROKEN - DefaultApi no longer exists
        return new DefaultApi(apiClient);                     // ❌ BROKEN
    }

    @Bean
    @ConditionalOnMissingBean
    public opencode.sdk.springboot.OpenCodeService openCodeService(DefaultApi defaultApi, ApiClient apiClient) {  // ❌ BROKEN parameter
        return new opencode.sdk.springboot.OpenCodeService(defaultApi, apiClient);  // ❌ BROKEN constructor call
    }

    private String createBasicAuthHeader(String username, String password) {
        String credentials = username + ":" + password;
        String encoded = Base64.getEncoder().encodeToString(credentials.getBytes());
        return "Basic " + encoded;
    }
}
```

### Analysis:

#### Imports:
| Import | Status |
|--------|--------|
| `opencode.sdk.api.DefaultApi` | ❌ **BROKEN** — `DefaultApi` does not exist in new SDK |
| `opencode.sdk.invoker.ApiClient` | ✅ OK |
| Spring Boot annotations | ✅ OK |

#### Beans Auto-Configured:
1. **`ApiClient`** — Created with base URL and Basic Auth interceptor. ✅ Still valid.
2. **`DefaultApi`** — Created from `ApiClient`. ❌ **BROKEN** — `DefaultApi` no longer exists.
3. **`OpenCodeService`** — Created from `DefaultApi` + `ApiClient`. ❌ **BROKEN** — depends on non-existent `DefaultApi`.

#### Private Methods:
- `createBasicAuthHeader(String username, String password)` — creates `Basic <base64>` header. ✅ Still valid.

#### Key Point:
The `apiClient()` bean itself is fine — it configures the base URL and auth. The problem starts at `defaultApi()` which tries to create a `DefaultApi` that no longer exists.

---

## File 3: `OpenCodeService.java`

**Path**: `src/main/java/opencode/sdk/springboot/OpenCodeService.java`
**Lines**: 33

### Complete Content:

```java
package opencode.sdk.springboot;

import opencode.sdk.api.DefaultApi;                          // ❌ BROKEN - DefaultApi no longer exists
import opencode.sdk.api.SessionApi;                          // ✅ OK - SessionApi still exists
import opencode.sdk.invoker.ApiClient;                       // ✅ OK
import opencode.sdk.invoker.ApiException;                    // ✅ OK
import opencode.sdk.model.GlobalHealth200Response;           // ✅ OK (presumed)
import org.springframework.stereotype.Service;

@Service
public class OpenCodeService {

    private final DefaultApi defaultApi;                      // ❌ BROKEN field type
    private final ApiClient apiClient;                        // ✅ OK

    public OpenCodeService(DefaultApi defaultApi, ApiClient apiClient) {  // ❌ BROKEN parameter
        this.defaultApi = defaultApi;
        this.apiClient = apiClient;
    }

    public GlobalHealth200Response getHealth() throws ApiException {
        return defaultApi.globalHealth();                     // ❌ BROKEN - calls DefaultApi method
    }

    public DefaultApi api() {                                 // ❌ BROKEN return type
        return defaultApi;
    }

    public SessionApi sessionApi() {                          // ✅ OK - creates SessionApi on-the-fly
        return new SessionApi(apiClient);
    }
}
```

### Analysis:

#### Imports:
| Import | Status |
|--------|--------|
| `opencode.sdk.api.DefaultApi` | ❌ **BROKEN** — does not exist |
| `opencode.sdk.api.SessionApi` | ✅ OK — `SessionApi` exists in new SDK |
| `opencode.sdk.invoker.ApiClient` | ✅ OK |
| `opencode.sdk.invoker.ApiException` | ✅ OK |
| `opencode.sdk.model.GlobalHealth200Response` | ✅ OK (needs verification) |
| `org.springframework.stereotype.Service` | ✅ OK |

#### Fields:
- `DefaultApi defaultApi` ❌ BROKEN
- `ApiClient apiClient` ✅ OK

#### Methods:
| Method | Signature | Status |
|--------|-----------|--------|
| Constructor | `OpenCodeService(DefaultApi, ApiClient)` | ❌ BROKEN |
| `getHealth()` | `GlobalHealth200Response getHealth() throws ApiException` | ❌ BROKEN — calls `defaultApi.globalHealth()` |
| `api()` | `DefaultApi api()` | ❌ BROKEN — returns non-existent type |
| `sessionApi()` | `SessionApi sessionApi()` | ✅ OK — creates `new SessionApi(apiClient)` on-the-fly |

#### How API instances are created/exposed:
- `OpenCodeService` stores a `DefaultApi` instance (passed from auto-configuration)
- The `api()` method exposes it directly for consumers to call any method
- The `sessionApi()` method creates a **new** `SessionApi` each time it's called (not cached)
- `getHealth()` delegates to `defaultApi.globalHealth()`

---

## File 4: Auto-Configuration Registration

**Path**: `src/main/resources/META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports`
**Content**: 
```
opencode.sdk.springboot.autoconfigure.OpenCodeAutoConfiguration
```

This registers the auto-configuration class for Spring Boot 3.x+ auto-detection. ✅ No changes needed here.

---

## The 22 New API Classes in the SDK

All are in `opencode.sdk.api` package. All follow the same constructor pattern:

```java
public XxxApi() {
    this(Configuration.getDefaultApiClient());
}

public XxxApi(ApiClient apiClient) {
    memberVarHttpClient = apiClient.getHttpClient();
    memberVarObjectMapper = apiClient.getObjectMapper();
    memberVarBaseUri = apiClient.getBaseUri();
    memberVarInterceptor = apiClient.getRequestInterceptor();
    memberVarReadTimeout = apiClient.getReadTimeout();
    memberVarResponseInterceptor = apiClient.getResponseInterceptor();
    memberVarAsyncResponseInterceptor = apiClient.getAsyncResponseInterceptor();
}
```

### Complete List of New API Classes:

| # | Class Name | Line Count | Key Methods (non-infrastructure) |
|---|-----------|------------|----------------------------------|
| 1 | `ConfigApi` | 581 | `configGet`, `configProviders`, `configUpdate` |
| 2 | `ControlApi` | ? | (needs inspection) |
| 3 | `EventApi` | ? | `eventSubscribe` |
| 4 | `ExperimentalApi` | ? | `experimentalWorkspaceCreate` |
| 5 | `FileApi` | 1043 | `fileList`, `fileRead`, `findText`, `findFiles`, `findSymbols`, `fileStatus` |
| 6 | `GlobalApi` | 846 | `globalConfigGet`, `globalConfigUpdate`, `globalDispose`, `globalEvent`, `globalHealth`, `globalUpgrade` |
| 7 | `InstanceApi` | 1812 | `instanceDispose`, `globalHealth` (and others) |
| 8 | `McpApi` | 1320 | `mcpStatus`, `mcpAdd`, `mcpAuthStart`, `mcpAuthCallback`, `mcpAuthRemove` |
| 9 | `PermissionApi` | ? | `permissionList`, `permissionReply`, `permissionRespond` |
| 10 | `ProjectApi` | ? | `projectList`, `projectCurrent`, `projectUpdate` |
| 11 | `ProviderApi` | ? | `providerList`, `providerOauthAuthorize`, `providerOauthCallback` |
| 12 | `PtyApi` | ? | `ptyList`, `ptyCreate`, `ptyGet`, `ptyUpdate`, `ptyRemove` |
| 13 | `PtyWsApi` | ? | (WebSocket-based) |
| 14 | `QuestionApi` | ? | `questionList`, `questionReply` |
| 15 | `SessionApi` | 4258 | `sessionList`, `sessionCreate`, `sessionGet`, `sessionUpdate`, `sessionDelete`, `sessionInit`, `sessionChildren`, `sessionFork`, `sessionRevert`, `sessionShare`, `sessionSummarize`, `sessionCommand`, `sessionShell`, `sessionAbort`, `sessionPrompt`, `sessionMessages`, `sessionTodo`, `sessionDiff` |
| 16 | `SyncApi` | ? | (needs inspection) |
| 17 | `TuiApi` | ? | (needs inspection) |
| 18 | `V2Api` | ? | (needs inspection) |
| 19 | `V2MessagesApi` | ? | (needs inspection) |
| 20 | `V2ModelsApi` | ? | (needs inspection) |
| 21 | `V2ProvidersApi` | ? | (needs inspection) |
| 22 | `WorkspaceApi` | ? | `worktreeList`, `worktreeCreate`, `worktreeRemove` |

**Note**: There is NO `DefaultApi` in the new SDK. The old monolithic `DefaultApi` has been split into these 22 domain-specific API classes.

---

## How Spring Boot Example App Uses OpenCodeService

The example app at `examples/spring-boot/` has 17 controllers that all inject `OpenCodeService`. They use it in two patterns:

### Pattern 1: `openCodeService.api()` (the majority)
This calls methods that were on `DefaultApi`. All of these are now **BROKEN** because `DefaultApi` doesn't exist.

Example controllers and their `openCodeService.api()` calls:
- **ConfigurationController**: `configGet`, `globalConfigGet`, `configUpdate`, `configProviders`
- **DevToolsController**: `lspStatus`, `formatterStatus`, `appLog`
- **EventStreamingController**: `eventSubscribe`, `globalEvent`
- **ExperimentalController**: `experimentalWorkspaceCreate`, `worktreeList`, `worktreeCreate`, `worktreeRemove`
- **FileOperationsController**: `fileList`, `fileRead`, `findText`, `findFiles`, `findSymbols`, `sessionDiff`, `fileStatus`
- **InstanceController**: `globalHealth`, `instanceDispose`
- **InteractiveController**: `questionList`, `questionReply`, `permissionList`, `permissionReply`, `permissionRespond`
- **McpController**: `mcpStatus`, `mcpAdd`, `mcpAuthStart`, `mcpAuthCallback`, `mcpAuthRemove`
- **MessageController**: `sessionMessages`, `sessionPrompt`, `sessionAbort`
- **ProjectController**: `projectList`, `projectCurrent`, `projectUpdate`
- **ProviderController**: `providerList`, `providerOauthAuthorize`, `providerOauthCallback`
- **PtyController**: `ptyList`, `ptyCreate`, `ptyGet`, `ptyUpdate`, `ptyRemove`
- **SessionAdvancedController**: `sessionFork`, `sessionRevert`, `sessionShare`, `sessionSummarize`, `sessionCommand`, `sessionShell`
- **SessionCrudController**: `sessionList`, `sessionCreate`, `sessionUpdate`, `sessionDelete`, `sessionInit`
- **SystemInfoController**: `globalHealth`, `appSkills`
- **TodoController**: `sessionTodo`
- **VcsController**: `vcsGet`

### Pattern 2: `openCodeService.sessionApi()` (2 usages only)
- **SessionCrudController**: `sessionApi().sessionGet(sessionId, null, null)`
- **SessionAdvancedController**: `sessionApi().sessionChildren(sessionId, null, null)`

### Method-to-New-API Mapping (from example usage)

| Old `DefaultApi` Method | New API Class |
|-------------------------|---------------|
| `configGet` | `ConfigApi` |
| `configProviders` | `ConfigApi` |
| `configUpdate` | `ConfigApi` |
| `globalConfigGet` | `GlobalApi` |
| `lspStatus` | `InstanceApi` or other |
| `formatterStatus` | `InstanceApi` or other |
| `appLog` | `InstanceApi` or other |
| `eventSubscribe` | `EventApi` |
| `globalEvent` | `GlobalApi` |
| `experimentalWorkspaceCreate` | `ExperimentalApi` or `WorkspaceApi` |
| `worktreeList` | `WorkspaceApi` |
| `worktreeCreate` | `WorkspaceApi` |
| `worktreeRemove` | `WorkspaceApi` |
| `fileList` | `FileApi` |
| `fileRead` | `FileApi` |
| `findText` | `FileApi` |
| `findFiles` | `FileApi` |
| `findSymbols` | `FileApi` |
| `fileStatus` | `FileApi` |
| `sessionDiff` | `SessionApi` |
| `globalHealth` | `GlobalApi` |
| `instanceDispose` | `InstanceApi` |
| `questionList` | `QuestionApi` |
| `questionReply` | `QuestionApi` |
| `permissionList` | `PermissionApi` |
| `permissionReply` | `PermissionApi` |
| `permissionRespond` | `PermissionApi` |
| `mcpStatus` | `McpApi` |
| `mcpAdd` | `McpApi` |
| `mcpAuthStart` | `McpApi` |
| `mcpAuthCallback` | `McpApi` |
| `mcpAuthRemove` | `McpApi` |
| `sessionMessages` | `SessionApi` |
| `sessionPrompt` | `SessionApi` |
| `sessionAbort` | `SessionApi` |
| `projectList` | `ProjectApi` |
| `projectCurrent` | `ProjectApi` |
| `projectUpdate` | `ProjectApi` |
| `providerList` | `ProviderApi` |
| `providerOauthAuthorize` | `ProviderApi` |
| `providerOauthCallback` | `ProviderApi` |
| `ptyList` | `PtyApi` |
| `ptyCreate` | `PtyApi` |
| `ptyGet` | `PtyApi` |
| `ptyUpdate` | `PtyApi` |
| `ptyRemove` | `PtyApi` |
| `sessionFork` | `SessionApi` |
| `sessionRevert` | `SessionApi` |
| `sessionShare` | `SessionApi` |
| `sessionSummarize` | `SessionApi` |
| `sessionCommand` | `SessionApi` |
| `sessionShell` | `SessionApi` |
| `sessionList` | `SessionApi` |
| `sessionCreate` | `SessionApi` |
| `sessionUpdate` | `SessionApi` |
| `sessionDelete` | `SessionApi` |
| `sessionInit` | `SessionApi` |
| `sessionGet` | `SessionApi` |
| `sessionChildren` | `SessionApi` |
| `sessionTodo` | `SessionApi` |
| `appSkills` | `GlobalApi` or other |
| `vcsGet` | `ControlApi` or other |

---

## Summary of Broken References

### Broken Imports (2 files, 2 broken import statements):
1. **`OpenCodeAutoConfiguration.java` line 3**: `import opencode.sdk.api.DefaultApi;`
2. **`OpenCodeService.java` line 3**: `import opencode.sdk.api.DefaultApi;`

### Broken Type References:
1. **`OpenCodeAutoConfiguration.java`**: `DefaultApi` used as return type (bean method), parameter type, constructor argument
2. **`OpenCodeService.java`**: `DefaultApi` used as field type, constructor parameter, return type of `api()` method

### Broken Method Calls:
1. **`OpenCodeService.getHealth()`**: calls `defaultApi.globalHealth()`
2. **`OpenCodeAutoConfiguration.defaultApi()`**: instantiates `new DefaultApi(apiClient)`

---

## What Needs to Change

### To support the 22 new API classes, the starter needs:

1. **`OpenCodeAutoConfiguration`** must be updated to:
   - Remove the `DefaultApi` bean
   - Add bean definitions for each of the 22 new API classes (or a factory approach)
   - All new API classes take `ApiClient` in their constructor — same pattern as old `DefaultApi`

2. **`OpenCodeService`** must be updated to:
   - Remove `DefaultApi` field and all references
   - Expose the new API classes instead
   - Options:
     - **Option A**: Add typed getter methods for each API (e.g., `configApi()`, `sessionApi()`, `fileApi()`, etc.) — each creates/lazily caches an instance
     - **Option B**: Store all 22 API instances as fields, inject from auto-configuration
     - **Option C**: Keep `ApiClient` as the only stored field and provide factory methods that create API instances on demand (like current `sessionApi()`)

3. **Spring Boot Example** (downstream impact):
   - All 17 controllers call `openCodeService.api().someMethod()` — these will need to change to the appropriate typed API accessor (e.g., `openCodeService.configApi().configGet(...)`)
   - Only `SessionCrudController` and `SessionAdvancedController` already use `sessionApi()` directly

4. **`OpenCodeProperties`**: No changes needed — it only deals with base URL and auth.

5. **Auto-configuration registration**: No changes needed.

### Recommended Approach:

**Option C (ApiClient + factory methods)** is likely the cleanest approach because:
- It mirrors the current `sessionApi()` pattern already in use
- API instances are cheap to create (they just copy references from `ApiClient`)
- It avoids bloating auto-configuration with 22 bean definitions
- Lazy creation avoids instantiating unused API classes
- Consider caching with lazy initialization for frequently-used APIs
