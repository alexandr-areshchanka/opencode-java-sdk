# Task 03: Fix Spring Boot Examples

**Type:** Code Modification
**Suggested agent:** Code

## Goal
Update all 16 spring-boot controller files to use the new typed API accessors from `OpenCodeService` instead of the removed `DefaultApi`-based `api()` method, and fix parameter count mismatches and type issues.

## Why This Task Exists
All 16 controllers call `openCodeService.api().someMethod()` where `api()` returned `DefaultApi` which no longer exists. After Task 01 updates the starter, controllers must use the new `openCodeService.xxxApi()` accessors.

## Spec Coverage
- Requirements: R3, R4
- Scenarios: S3

## Required Inputs
- `.tasks/migration-review-examples/research/spring-boot-findings.md` — File-by-file analysis with exact method calls, imports, and issues for all 17 files
- `.tasks/migration-review-examples/research/sdk-api-surface.md` — All 22 API classes with exact method signatures
- Task 01 must be completed first — the starter must provide the new API accessors

## Files/Areas
All files in `examples/spring-boot/src/main/java/opencode/sdk/springboot/controller/`:

### Cross-API mixing controllers (call methods from 2+ API classes):
- `ConfigurationController.java` — `ConfigApi` + `GlobalApi`
- `EventStreamingController.java` — `EventApi` + `GlobalApi`
- `ExperimentalController.java` — `WorkspaceApi` + `ExperimentalApi`
- `FileOperationsController.java` — `FileApi` + `SessionApi`
- `InstanceController.java` — `GlobalApi` + `InstanceApi`
- `InteractiveController.java` — `QuestionApi` + `PermissionApi` + `SessionApi`
- `SystemInfoController.java` — `GlobalApi` + `InstanceApi`

### Single-API controllers:
- `DevToolsController.java` — `InstanceApi`
- `McpController.java` — `McpApi`
- `MessageController.java` — `SessionApi` (has param mismatch)
- `ProjectController.java` — `ProjectApi`
- `ProviderController.java` — `ProviderApi`
- `PtyController.java` — `PtyApi`
- `SessionAdvancedController.java` — `SessionApi` (has return type mismatch)
- `SessionCrudController.java` — `SessionApi` (has param mismatch)
- `TodoController.java` — `SessionApi`
- `VcsController.java` — `InstanceApi`

### Application class (no changes needed):
- `OpenCodeSpringBootApplication.java` — no direct SDK usage

## Constraints / Non-Goals
- Lombok is used in spring-boot examples (`@RequiredArgsConstructor`, `@Slf4j`) — keep using it
- Do NOT change REST endpoint mappings or response structures
- Fix only compile issues, not logic bugs (except the `instanceDispose` path variable bug which is trivial)
- Do NOT create tests (project policy: no tests unless explicitly asked)

## Output Artifacts
- N/A — direct file edits

## What to Do

### General Pattern for Each Controller
Replace `openCodeService.api().methodName(...)` with `openCodeService.xxxApi().methodName(...)` using the correct API accessor.

### Specific Fixes

#### `ConfigurationController.java`
```java
// Old: openCodeService.api().configGet(null, null)
// New: openCodeService.configApi().configGet(null, null)

// Old: openCodeService.api().globalConfigGet()
// New: openCodeService.globalApi().globalConfigGet()

// Old: openCodeService.api().configUpdate(null, null, config)
// New: openCodeService.configApi().configUpdate(null, null, config)

// Old: openCodeService.api().configProviders(null, null)
// New: openCodeService.configApi().configProviders(null, null)
```

#### `MessageController.java` — Parameter mismatch
```java
// Old: openCodeService.api().sessionMessages(sessionId, null, null, null) — 4 args
// New: openCodeService.sessionApi().sessionMessages(sessionId, null, null, null, null) — 5 args (added `before`)
```

#### `SessionCrudController.java` — Parameter mismatch
```java
// Old: openCodeService.api().sessionList(null, null, null, null, null, null) — 6 args
// New: openCodeService.sessionApi().sessionList(null, null, null, null, null, null, null, null) — 8 args (added `search`, `limit`)
```

#### `SessionAdvancedController.java` — Return type mismatch
```java
// Old: AssistantMessage message = openCodeService.api().sessionShell(sessionId, null, null, request)
// New: SessionShell200Response response = openCodeService.sessionApi().sessionShell(sessionId, null, null, request)
// Note: Also update the import — remove AssistantMessage, add SessionShell200Response
```

#### `InstanceController.java` — Logic bug
```java
// The instanceDispose path variable is unused. Fix is trivial but not strictly a compile issue.
// Optional: change instanceDispose(null, null) to use the instanceId if applicable
```

#### All other controllers
Replace `openCodeService.api()` with the correct typed accessor based on which API class the method belongs to.

### API-to-Accessor Mapping Reference
| API Class | Accessor Method | Controllers Using It |
|-----------|----------------|---------------------|
| `ConfigApi` | `openCodeService.configApi()` | ConfigurationController |
| `EventApi` | `openCodeService.eventApi()` | EventStreamingController |
| `ExperimentalApi` | `openCodeService.experimentalApi()` | ExperimentalController, VcsExample |
| `FileApi` | `openCodeService.fileApi()` | FileOperationsController |
| `GlobalApi` | `openCodeService.globalApi()` | ConfigurationController, EventStreamingController, InstanceController, SystemInfoController |
| `InstanceApi` | `openCodeService.instanceApi()` | DevToolsController, InstanceController, SystemInfoController, VcsController |
| `McpApi` | `openCodeService.mcpApi()` | McpController |
| `PermissionApi` | `openCodeService.permissionApi()` | InteractiveController |
| `ProjectApi` | `openCodeService.projectApi()` | ProjectController |
| `ProviderApi` | `openCodeService.providerApi()` | ProviderController |
| `PtyApi` | `openCodeService.ptyApi()` | PtyController |
| `QuestionApi` | `openCodeService.questionApi()` | InteractiveController |
| `SessionApi` | `openCodeService.sessionApi()` | FileOperationsController, InteractiveController, MessageController, SessionAdvancedController, SessionCrudController, TodoController |
| `WorkspaceApi` | `openCodeService.workspaceApi()` | ExperimentalController |

## Expected Output
- All 16 controller files compile without errors
- No references to `DefaultApi` or `openCodeService.api()` remain
- All method calls use correct parameter counts
- Return types match generated signatures

## Acceptance Criteria
- [ ] Zero calls to `openCodeService.api()` in any controller
- [ ] Zero imports of `opencode.sdk.api.DefaultApi` in any file
- [ ] `MessageController.sessionMessages` passes 5 parameters
- [ ] `SessionCrudController.sessionList` passes 8 parameters
- [ ] `SessionAdvancedController.sessionShell` uses `SessionShell200Response` return type
- [ ] `mvn compile -pl examples/spring-boot -am` succeeds
- [ ] Covered requirements and scenarios are satisfied
- [ ] I've created a git commit for this task
