# Spring Boot Examples — Compile Issue Analysis

## Summary

**17 Java source files** found in `examples/spring-boot/src/main/java/`:
- 1 application class (`OpenCodeSpringBootApplication.java`)
- 16 REST controller classes in the `controller/` package

### CRITICAL FINDING: `DefaultApi` Does Not Exist

The OpenAPI Generator creates **tag-based API classes** — one per unique tag in the OpenAPI spec. Since every operation in `sdk/openapi.json` has a tag, **no `DefaultApi` class is generated**. However:

- The **Spring Boot Starter** (`opencode-spring-boot-starter`) imports and depends on `opencode.sdk.api.DefaultApi`
- The **`OpenCodeService`** exposes `api()` returning `DefaultApi`
- **ALL 16 controllers** call `openCodeService.api().*()` to invoke SDK methods

**Impact**: The starter itself cannot compile, and consequently none of the examples can compile either.

### Generated API Classes (22 total, NO DefaultApi)

| API Class | Tag | Example Operations |
|-----------|-----|-------------------|
| `ConfigApi` | config | configGet, configUpdate, configProviders |
| `ControlApi` | control | authSet, authRemove |
| `EventApi` | event | eventSubscribe |
| `ExperimentalApi` | experimental | worktreeList, worktreeCreate, worktreeRemove, worktreeReset |
| `FileApi` | file | fileList, fileRead, fileStatus, findText, findFiles, findSymbols |
| `GlobalApi` | global | globalHealth, globalEvent, globalConfigGet, globalConfigUpdate, globalDispose, globalUpgrade |
| `InstanceApi` | instance | lspStatus, formatterStatus, appLog, instanceDispose, pathGet, vcsGet, vcsStatus, vcsDiff, vcsApply |
| `McpApi` | mcp | mcpStatus, mcpAdd, mcpAuthStart, mcpAuthRemove, mcpAuthCallback |
| `PermissionApi` | permission | permissionList, permissionReply |
| `ProjectApi` | project | projectList, projectCurrent, projectUpdate, projectInitGit |
| `ProviderApi` | provider | providerList, providerOauthAuthorize, providerOauthCallback |
| `PtyApi` | pty | ptyList, ptyCreate, ptyGet, ptyUpdate, ptyRemove, ptyShells |
| `QuestionApi` | question | questionList, questionReply, questionReject |
| `SessionApi` | session | sessionList, sessionCreate, sessionGet, sessionDelete, sessionUpdate, sessionChildren, sessionTodo, sessionDiff, sessionMessages, sessionPrompt, sessionFork, sessionAbort, sessionInit, sessionShare, sessionSummarize, sessionCommand, sessionShell, sessionRevert, **permissionRespond** |
| `SyncApi` | sync | syncStart, syncReplay, syncSteal |
| `WorkspaceApi` | workspace | experimentalWorkspaceCreate, experimentalWorkspaceList, experimentalWorkspaceRemove |

---

## File-by-File Analysis

### 1. `OpenCodeSpringBootApplication.java` (13 lines)

**Imports**: Spring Boot standard (`SpringApplication`, `SpringBootApplication`)
**SDK/Starter usage**: None directly
**Compile issues**: ⚠️ **Indirect** — depends on the starter auto-configuration which itself cannot compile due to missing `DefaultApi`

---

### 2. `ConfigurationController.java` (48 lines)

**Imports**:
- `jakarta.validation.Valid`
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.Config`
- `opencode.sdk.model.ConfigProviders200Response`
- `opencode.sdk.springboot.OpenCodeService`
- Spring Web annotations

**SDK API calls via `openCodeService.api()`**:
- `configGet(null, null)` → should be on `ConfigApi`
- `globalConfigGet()` → should be on `GlobalApi`
- `configUpdate(null, null, config)` → should be on `ConfigApi`
- `configProviders(null, null)` → should be on `ConfigApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()` resolve to `DefaultApi` which doesn't exist
- ❌ Cross-API mixing: `globalConfigGet()` is on `GlobalApi` but `configGet()` is on `ConfigApi` — they can't both come from `openCodeService.api()`

---

### 3. `DevToolsController.java` (42 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.AppLogRequest`
- `opencode.sdk.model.FormatterStatus`
- `opencode.sdk.model.LSPStatus`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `lspStatus(null, null)` → `InstanceApi`
- `formatterStatus(null, null)` → `InstanceApi`
- `appLog(null, null, request)` → `InstanceApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`

---

### 4. `EventStreamingController.java` (30 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.Event`
- `opencode.sdk.model.GlobalEvent`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `eventSubscribe(null, null)` → `EventApi`
- `globalEvent()` → `GlobalApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`
- ❌ Cross-API mixing: `eventSubscribe` is on `EventApi`, `globalEvent` is on `GlobalApi`

---

### 5. `ExperimentalController.java` (38 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.*`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `experimentalWorkspaceCreate(null, null, request)` → `WorkspaceApi`
- `worktreeList(null, null)` → `ExperimentalApi`
- `worktreeCreate(null, null, input)` → `ExperimentalApi`
- `worktreeRemove(null, null, input)` → `ExperimentalApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`
- ❌ Cross-API mixing: `experimentalWorkspaceCreate` is on `WorkspaceApi`, others on `ExperimentalApi`

---

### 6. `FileOperationsController.java` (80 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.*` (FileNode, FileContent, FindText200ResponseInner, Symbol, FileDiff, ModelFile)
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `fileList(path, directory, workspace)` → `FileApi` — 3 args, matches generated signature ✓
- `fileRead(path, directory, workspace)` → `FileApi` — 3 args ✓
- `findText(pattern, directory, workspace)` → `FileApi` — 3 args ✓
- `findFiles(query, directory, workspace, dirs, type, limit)` → `FileApi` — 6 args, matches ✓
- `findSymbols(query, directory, workspace)` → `FileApi` — 3 args ✓
- `sessionDiff(sessionId, directory, workspace, messageId)` → `SessionApi` — 4 args ✓
- `fileStatus(directory, workspace)` → `FileApi` — 2 args ✓

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`
- ❌ Cross-API mixing: `sessionDiff` is on `SessionApi`, file operations on `FileApi`

---

### 7. `InstanceController.java` (32 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.GlobalHealth200Response`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `globalHealth()` → `GlobalApi` — 0 args ✓
- `instanceDispose(null, null)` → `InstanceApi` — 2 args ✓

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`
- ⚠️ **Logic bug**: `disposeInstance(@PathVariable String instanceId)` takes `instanceId` but doesn't use it — calls `instanceDispose(null, null)` ignoring the path variable
- ❌ Cross-API mixing: `globalHealth` on `GlobalApi`, `instanceDispose` on `InstanceApi`

---

### 8. `InteractiveController.java` (69 lines)

**Imports**:
- `jakarta.validation.Valid`
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.*` (QuestionRequest, QuestionReplyRequest, PermissionRequest, PermissionReplyRequest, PermissionRespondRequest)
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `questionList(null, null)` → `QuestionApi`
- `questionReply(requestId, null, null, request)` → `QuestionApi`
- `permissionList(null, null)` → `PermissionApi`
- `permissionReply(requestId, null, null, request)` → `PermissionApi`
- `permissionRespond(sessionId, permissionId, null, null, request)` → `SessionApi` (tag "session")

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`
- ❌ Cross-API mixing: question ops on `QuestionApi`, permission ops on `PermissionApi`, `permissionRespond` on `SessionApi`

---

### 9. `McpController.java` (51 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.*` (MCPStatus, McpAddRequest, McpAuthStart200Response, McpAuthCallbackRequest, McpAuthRemove200Response)
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `mcpStatus(null, null)` → `McpApi` — 2 args ✓
- `mcpAdd(null, null, request)` → `McpApi`
- `mcpAuthStart(name, null, null)` → `McpApi`
- `mcpAuthCallback(name, null, null, request)` → `McpApi`
- `mcpAuthRemove(name, null, null)` → `McpApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`

---

### 10. `MessageController.java` (41 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`, `lombok.extern.slf4j.Slf4j`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.SessionMessages200ResponseInner`
- `opencode.sdk.model.SessionPrompt200Response`
- `opencode.sdk.model.SessionPromptRequest`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `sessionMessages(sessionId, null, null, null)` → `SessionApi` — **4 args, but generated has 5 params** `(sessionID, directory, workspace, limit, before)` — ❌ **parameter count mismatch**
- `sessionPrompt(sessionId, null, null, request)` → `SessionApi` — 4 args ✓
- `sessionAbort(sessionId, null, null)` → `SessionApi` — 3 args ✓ (returns `Boolean`, return value ignored — OK)

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`
- ❌ **`sessionMessages` parameter mismatch**: 4 args passed, 5 expected (missing `before` parameter)

---

### 11. `ProjectController.java` (34 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.model.Project`
- `opencode.sdk.model.ProjectUpdateRequest`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `projectList(null, null)` → `ProjectApi`
- `projectCurrent(null, null)` → `ProjectApi`
- `projectUpdate(currentProject.getId(), null, null, request)` → `ProjectApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`
- ⚠️ **Style issue**: methods throw `Exception` instead of `ApiException`

---

### 12. `ProviderController.java` (61 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.ProviderAuthAuthorization`
- `opencode.sdk.model.ProviderList200Response`
- `opencode.sdk.model.ProviderOauthAuthorizeRequest`
- `opencode.sdk.model.ProviderOauthCallbackRequest`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `providerList(null, null)` → `ProviderApi` — returns `ProviderList200Response` ✓
- `providerOauthAuthorize(provider, null, null, request)` → `ProviderApi`
- `providerOauthCallback(provider, null, null, request)` → `ProviderApi`

**Model usage**:
- `response.getAll().stream()` — `ProviderList200Response` has `getAll()` method (schema has `all` property) ✓

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`

---

### 13. `PtyController.java` (45 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.Pty`
- `opencode.sdk.model.PtyCreateRequest`
- `opencode.sdk.model.PtyUpdateRequest`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `ptyList(null, null)` → `PtyApi`
- `ptyCreate(null, null, request)` → `PtyApi`
- `ptyGet(id, null, null)` → `PtyApi`
- `ptyUpdate(id, null, null, request)` → `PtyApi`
- `ptyRemove(id, null, null)` → `PtyApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`

---

### 14. `SessionAdvancedController.java` (96 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.*` (Session, SessionForkRequest, SessionRevertRequest, SessionSummarizeRequest, SessionCommandRequest, SessionShellRequest, SessionPrompt200Response, AssistantMessage)
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `openCodeService.api().sessionFork(sessionId, null, null, request)` → `SessionApi` — 4 args ✓
- `openCodeService.api().sessionRevert(sessionId, null, null, request)` → `SessionApi` — 4 args ✓
- `openCodeService.api().sessionShare(sessionId, null, null)` → `SessionApi` — 3 args ✓
- `openCodeService.api().sessionSummarize(sessionId, null, null, request)` → `SessionApi` — 4 args ✓
- `openCodeService.sessionApi().sessionChildren(sessionId, null, null)` → `SessionApi` — 3 args ✓ (correctly uses `sessionApi()`)
- `openCodeService.api().sessionCommand(sessionId, null, null, request)` → `SessionApi`
- `openCodeService.api().sessionShell(sessionId, null, null, request)` → `SessionApi` — **❌ return type mismatch**: method returns `SessionShell200Response`, code assigns to `AssistantMessage`

**Compile issues**:
- ❌ **`DefaultApi` not found** — most calls via `openCodeService.api()`
- ❌ **`sessionShell` return type mismatch**: `SessionShell200Response` ≠ `AssistantMessage`
  - Generated: `public SessionShell200Response sessionShell(String sessionID, String directory, String workspace, SessionShellRequest sessionShellRequest)`
  - Example: `AssistantMessage message = openCodeService.api().sessionShell(sessionId, null, null, request);`

---

### 15. `SessionCrudController.java` (76 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.Session`
- `opencode.sdk.model.SessionCreateRequest`
- `opencode.sdk.model.SessionInitRequest`
- `opencode.sdk.model.SessionUpdateRequest`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `openCodeService.api().sessionList(null, null, null, null, null, null)` → `SessionApi` — **❌ 6 args, but generated has 8 params** `(directory, workspace, scope, path, roots, start, search, limit)`
- `openCodeService.api().sessionCreate(null, null, request)` → `SessionApi` — 3 args ✓
- `openCodeService.sessionApi().sessionGet(sessionId, null, null)` → `SessionApi` — 3 args ✓ (correctly uses `sessionApi()`)
- `openCodeService.api().sessionUpdate(sessionId, null, null, request)` → `SessionApi` — 4 args ✓
- `openCodeService.api().sessionDelete(sessionId, null, null)` → `SessionApi` — 3 args ✓
- `openCodeService.api().sessionInit(sessionId, null, null, request)` → `SessionApi` — 4 args ✓

**Compile issues**:
- ❌ **`DefaultApi` not found** — most calls via `openCodeService.api()`
- ❌ **`sessionList` parameter mismatch**: 6 args passed, 8 expected (missing `search` and `limit` parameters)

---

### 16. `SystemInfoController.java` (31 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.AppSkills200ResponseInner`
- `opencode.sdk.model.GlobalHealth200Response`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `globalHealth()` → `GlobalApi` — 0 args ✓
- `appSkills(null, null)` → `InstanceApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — all calls via `openCodeService.api()`
- ❌ Cross-API mixing: `globalHealth` on `GlobalApi`, `appSkills` on `InstanceApi`

---

### 17. `TodoController.java` (26 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.Todo`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `sessionTodo(sessionId, null, null)` → `SessionApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — call via `openCodeService.api()`

---

### 18. `VcsController.java` (23 lines)

**Imports**:
- `lombok.RequiredArgsConstructor`
- `opencode.sdk.invoker.ApiException`
- `opencode.sdk.model.VcsInfo`
- `opencode.sdk.springboot.OpenCodeService`

**SDK API calls**:
- `vcsGet(null, null)` → `InstanceApi`

**Compile issues**:
- ❌ **`DefaultApi` not found** — call via `openCodeService.api()`

---

## Consolidated List of All Compile Issues

### Issue 1: `DefaultApi` Does Not Exist — BLOCKING ALL COMPILATION

**Affected files**: `OpenCodeAutoConfiguration.java`, `OpenCodeService.java` (starter), and ALL 16 controllers

**Root cause**: The OpenAPI Generator for Java (with `library: native`) creates one API class per tag. Since all operations in `sdk/openapi.json` have tags, no `DefaultApi` class is generated. The generated API classes are:

- `ConfigApi`, `ControlApi`, `EventApi`, `ExperimentalApi`, `FileApi`, `GlobalApi`, `InstanceApi`, `McpApi`, `PermissionApi`, `ProjectApi`, `ProviderApi`, `PtyApi`, `PtyWsApi`, `QuestionApi`, `SessionApi`, `SyncApi`, `TuiApi`, `V2Api`, `V2MessagesApi`, `V2ModelsApi`, `V2ProvidersApi`, `WorkspaceApi`

**Fix required**: Either:
1. Update the starter to provide access to all tag-based API classes (e.g., add `configApi()`, `sessionApi()`, `globalApi()`, etc.), OR
2. Configure the OpenAPI Generator to produce a single `DefaultApi` with all operations (e.g., by removing/merging all tags in the spec), OR
3. Create a custom facade/wrapper that delegates to all tag-specific API classes

### Issue 2: `sessionMessages` Parameter Count Mismatch

**File**: `MessageController.java` (line ~37)
**Call**: `sessionMessages(sessionId, null, null, null)` — 4 args
**Generated**: `sessionMessages(String sessionID, String directory, String workspace, Integer limit, String before)` — 5 params
**Missing**: `before` parameter (5th argument)

### Issue 3: `sessionList` Parameter Count Mismatch

**File**: `SessionCrudController.java` (line ~37)
**Call**: `sessionList(null, null, null, null, null, null)` — 6 args
**Generated**: `sessionList(String directory, String workspace, String scope, String path, ExperimentalSessionListRootsParameter roots, BigDecimal start, String search, BigDecimal limit)` — 8 params
**Missing**: `search` and `limit` parameters (7th and 8th arguments)

### Issue 4: `sessionShell` Return Type Mismatch

**File**: `SessionAdvancedController.java` (line ~91)
**Call**: `AssistantMessage message = openCodeService.api().sessionShell(...)`
**Generated return**: `SessionShell200Response` (has `info: Message` and `parts: List<Part>` fields)
**Expected**: `AssistantMessage`
**These are incompatible types** — `SessionShell200Response` is not assignable to `AssistantMessage`

### Issue 5: Cross-API Mixing in Single Controllers

Multiple controllers call methods from different tag-based API classes via the same `openCodeService.api()` call. Even after fixing `DefaultApi`, the code would need separate API accessors:

| Controller | Methods from different APIs |
|------------|---------------------------|
| `ConfigurationController` | `ConfigApi` + `GlobalApi` |
| `EventStreamingController` | `EventApi` + `GlobalApi` |
| `ExperimentalController` | `WorkspaceApi` + `ExperimentalApi` |
| `FileOperationsController` | `FileApi` + `SessionApi` |
| `InstanceController` | `GlobalApi` + `InstanceApi` |
| `InteractiveController` | `QuestionApi` + `PermissionApi` + `SessionApi` |
| `SystemInfoController` | `GlobalApi` + `InstanceApi` |

### Issue 6: Logic Bug — `instanceDispose` ignores `instanceId`

**File**: `InstanceController.java`
**Method**: `disposeInstance(@PathVariable String instanceId)` calls `instanceDispose(null, null)` without using the `instanceId` parameter

### Issue 7: `ProjectController` throws generic `Exception`

**File**: `ProjectController.java`
All methods declare `throws Exception` instead of `throws ApiException`. Not a compile error but inconsistent with all other controllers.

---

## Model Import Summary (All referenced model classes)

The following model classes are imported across all controllers. These should all exist in `opencode.sdk.model` package as auto-generated classes:

| Model Class | Used In |
|-------------|---------|
| `Config` | ConfigurationController |
| `ConfigProviders200Response` | ConfigurationController |
| `AppLogRequest` | DevToolsController |
| `FormatterStatus` | DevToolsController |
| `LSPStatus` | DevToolsController |
| `Event` | EventStreamingController |
| `GlobalEvent` | EventStreamingController |
| `Workspace` | ExperimentalController |
| `ExperimentalWorkspaceCreateRequest` | ExperimentalController |
| `WorktreeCreateInput` | ExperimentalController |
| `WorktreeRemoveInput` | ExperimentalController |
| `FileNode` | FileOperationsController |
| `FileContent` | FileOperationsController |
| `FindText200ResponseInner` | FileOperationsController |
| `Symbol` | FileOperationsController |
| `FileDiff` | FileOperationsController |
| `ModelFile` | FileOperationsController |
| `GlobalHealth200Response` | InstanceController, SystemInfoController |
| `QuestionRequest` | InteractiveController |
| `QuestionReplyRequest` | InteractiveController |
| `PermissionRequest` | InteractiveController |
| `PermissionReplyRequest` | InteractiveController |
| `PermissionRespondRequest` | InteractiveController |
| `MCPStatus` | McpController |
| `McpAddRequest` | McpController |
| `McpAuthStart200Response` | McpController |
| `McpAuthCallbackRequest` | McpController |
| `McpAuthRemove200Response` | McpController |
| `SessionMessages200ResponseInner` | MessageController |
| `SessionPrompt200Response` | MessageController, SessionAdvancedController |
| `SessionPromptRequest` | MessageController |
| `Project` | ProjectController |
| `ProjectUpdateRequest` | ProjectController |
| `ProviderAuthAuthorization` | ProviderController |
| `ProviderList200Response` | ProviderController |
| `ProviderOauthAuthorizeRequest` | ProviderController |
| `ProviderOauthCallbackRequest` | ProviderController |
| `Pty` | PtyController |
| `PtyCreateRequest` | PtyController |
| `PtyUpdateRequest` | PtyController |
| `Session` | SessionCrudController, SessionAdvancedController |
| `SessionCreateRequest` | SessionCrudController |
| `SessionInitRequest` | SessionCrudController |
| `SessionUpdateRequest` | SessionCrudController |
| `SessionForkRequest` | SessionAdvancedController |
| `SessionRevertRequest` | SessionAdvancedController |
| `SessionSummarizeRequest` | SessionAdvancedController |
| `SessionCommandRequest` | SessionAdvancedController |
| `SessionShellRequest` | SessionAdvancedController |
| `AssistantMessage` | SessionAdvancedController |
| `AppSkills200ResponseInner` | SystemInfoController |
| `Todo` | TodoController |
| `VcsInfo` | VcsController |

---

## Recommended Fix Strategy

1. **Starter-level**: Update `OpenCodeService` to provide typed accessors for each API class, or create a unified API facade that wraps all tag-specific APIs behind one interface. Update `OpenCodeAutoConfiguration` to instantiate all required API classes.

2. **Example-level**: Update all controller method calls to use the correct API accessor (e.g., `openCodeService.configApi().configGet(...)` instead of `openCodeService.api().configGet(...)`).

3. **Parameter mismatches**: Update `sessionList` calls to pass all 8 parameters and `sessionMessages` calls to pass all 5 parameters.

4. **Type mismatch**: Update `SessionAdvancedController.sessionShell()` to use `SessionShell200Response` instead of `AssistantMessage`.
