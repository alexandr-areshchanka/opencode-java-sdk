# Task 01: Update Spring Boot Starter — Results

## Status: ✅ COMPLETE

All changes implemented and compilation verified.

## Changes Made

### 1. `OpenCodeService.java` (131 lines, was 33 lines)

**File**: `opencode-spring-boot-starter/src/main/java/opencode/sdk/springboot/OpenCodeService.java`

**Removed**:
- `DefaultApi defaultApi` field
- `DefaultApi defaultApi` constructor parameter
- `api()` method (returned `DefaultApi`)
- Import of `opencode.sdk.api.DefaultApi`

**Updated**:
- Constructor now takes only `ApiClient apiClient`
- `getHealth()` now uses `new GlobalApi(apiClient).globalHealth()` instead of `defaultApi.globalHealth()`

**Added** — 22 typed API accessor methods (factory pattern, each creates a new API instance):
1. `configApi()` → `ConfigApi`
2. `controlApi()` → `ControlApi`
3. `eventApi()` → `EventApi`
4. `experimentalApi()` → `ExperimentalApi`
5. `fileApi()` → `FileApi`
6. `globalApi()` → `GlobalApi`
7. `instanceApi()` → `InstanceApi`
8. `mcpApi()` → `McpApi`
9. `permissionApi()` → `PermissionApi`
10. `projectApi()` → `ProjectApi`
11. `providerApi()` → `ProviderApi`
12. `ptyApi()` → `PtyApi`
13. `ptyWsApi()` → `PtyWsApi`
14. `questionApi()` → `QuestionApi`
15. `sessionApi()` → `SessionApi` (already existed, kept)
16. `syncApi()` → `SyncApi`
17. `tuiApi()` → `TuiApi`
18. `v2Api()` → `V2Api`
19. `v2MessagesApi()` → `V2MessagesApi`
20. `v2ModelsApi()` → `V2ModelsApi`
21. `v2ProvidersApi()` → `V2ProvidersApi`
22. `workspaceApi()` → `WorkspaceApi`

### 2. `OpenCodeAutoConfiguration.java` (46 lines, was 53 lines)

**File**: `opencode-spring-boot-starter/src/main/java/opencode/sdk/springboot/autoconfigure/OpenCodeAutoConfiguration.java`

**Removed**:
- Import of `opencode.sdk.api.DefaultApi`
- `defaultApi()` bean method entirely
- `DefaultApi defaultApi` parameter from `openCodeService()` bean

**Updated**:
- `openCodeService()` bean now takes only `ApiClient apiClient` and calls `new OpenCodeService(apiClient)`

### 3. Unchanged Files
- `OpenCodeProperties.java` — no changes
- `META-INF/spring/org.springframework.boot.autoconfigure.AutoConfiguration.imports` — no changes

## Build Verification

```
mvn install -pl opencode-spring-boot-starter -am -DskipTests

Reactor Summary:
  OpenCode Java SDK .................................. SUCCESS [  0.183 s]
  OpenCode SDK ....................................... SUCCESS [ 28.138 s]
  OpenCode Spring Boot Starter ....................... SUCCESS [  1.041 s]
  BUILD SUCCESS
```

## Acceptance Criteria Checklist

- [x] No import of `opencode.sdk.api.DefaultApi` in any starter file
- [x] All 22 API classes have accessor methods in `OpenCodeService`
- [x] `getHealth()` uses `GlobalApi` instead of `DefaultApi`
- [x] `OpenCodeAutoConfiguration` creates `ApiClient` and `OpenCodeService` beans only
- [x] `mvn compile -pl opencode-spring-boot-starter` succeeds (via `install -am`)
- [x] No Lombok used
- [x] No inner classes
- [x] Factory method pattern (lazy, no cached fields) for all API accessors
