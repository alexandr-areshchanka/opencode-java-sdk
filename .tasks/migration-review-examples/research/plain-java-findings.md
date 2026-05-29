# Plain Java Examples — Migration Review Findings

## Summary

The `examples/plain-java/` module contains **38 Java source files**:
- **20 example classes** in `opencode.examples.plainjava` package
- **18 testing/utility classes** in `opencode.examples.plainjava.testing` package

**CRITICAL FINDING: ALL 20 example files and 3 testing files WILL NOT COMPILE** because the SDK's generated API structure has changed from a single `DefaultApi` class to 22 individual API classes.

---

## CRITICAL ISSUE #1: `DefaultApi` class no longer exists

### Impact: ALL example files fail to compile

The old SDK had a single `opencode.sdk.api.DefaultApi` containing all 50+ API endpoint methods. The new SDK generates **22 individual API classes** instead:

| New API Class | Old methods now in this class |
|---|---|
| `ConfigApi` | `configGet`, `configProviders`, `configUpdate` |
| `ControlApi` | `appLog`, `authRemove`, `authSet` |
| `EventApi` | `eventSubscribe`, `globalEvent` (possibly) |
| `ExperimentalApi` | `experimentalSessionList`, `experimentalResourceList` |
| `FileApi` | `fileList`, `fileRead`, `fileStatus`, `findFiles`, `findText`, `findSymbols` |
| `GlobalApi` | `globalHealth`, `globalConfigGet`, `globalConfigUpdate`, `globalDispose`, `globalEvent` |
| `InstanceApi` | `appAgents`, `appSkills`, `commandList`, `formatterStatus`, `lspStatus`, `instanceDispose`, `vcsGet`, `vcsDiff`, `vcsDiffRaw`, `vcsStatus`, `vcsApply`, `pathGet` |
| `McpApi` | `mcpStatus`, `mcpAdd`, `mcpConnect`, `mcpAuthStart`, `mcpAuthCallback`, `mcpAuthRemove` |
| `PermissionApi` | `permissionList`, `permissionRespond` |
| `ProjectApi` | `projectList`, `projectCurrent`, `projectUpdate`, `projectInitGit` |
| `ProviderApi` | `providerList`, `providerAuth`, `providerOauthAuthorize`, `providerOauthCallback` |
| `PtyApi` | `ptyList`, `ptyCreate`, `ptyGet`, `ptyUpdate`, `ptyRemove`, `ptyConnectToken`, `ptyShells` |
| `PtyWsApi` | (WebSocket PTY operations) |
| `QuestionApi` | `questionList`, `questionReply`, `questionReject` |
| `SessionApi` | `sessionList`, `sessionCreate`, `sessionGet`, `sessionDelete`, `sessionUpdate`, `sessionFork`, `sessionChildren`, `sessionShare`, `sessionUnshare`, `sessionSummarize`, `sessionAbort`, `sessionRevert`, `sessionUnrevert`, `sessionPrompt`, `sessionMessages`, `sessionTodo`, `sessionCommand`, `sessionDiff`, `sessionDeleteMessage`, `partDelete`, `partUpdate`, `permissionRespond` |
| `SyncApi` | (Sync/event replay operations) |
| `TuiApi` | (TUI control operations) |
| `V2Api` | (V2 API operations) |
| `V2MessagesApi` | (V2 messages) |
| `V2ModelsApi` | (V2 models) |
| `V2ProvidersApi` | (V2 providers) |
| `WorkspaceApi` | `experimentalWorkspaceList`, `experimentalWorkspaceCreate`, `experimentalWorkspaceRemove`, `experimentalWorkspaceStatus`, `experimentalWorkspaceAdapterList`, `experimentalWorkspaceSyncList`, `experimentalWorkspaceWarp` |

### Files Affected

**All 20 example files** import `opencode.sdk.api.DefaultApi`:
1. `ConfigurationExample.java`
2. `DevToolsExample.java`
3. `EventStreamingExample.java`
4. `ExperimentalExample.java`
5. `FileOperationsExample.java`
6. `InstanceExample.java`
7. `InteractiveExample.java`
8. `Main.java`
9. `McpExample.java`
10. `MessageExample.java`
11. `ProjectExample.java`
12. `ProviderExample.java`
13. `PtyExample.java`
14. `SessionAdvancedExample.java`
15. `SessionCrudExample.java`
16. `SystemInfoExample.java`
17. `TodoExample.java`
18. `VcsExample.java`

**Testing files that import `DefaultApi`:**
- `TestExecutor.java` — creates `new DefaultApi(apiClient)`
- `CleanupManager.java` — stores `DefaultApi api` field, calls `api.sessionDelete()`
- `ExampleContext.java` — stores `DefaultApi defaultApi` field

**Testing files that import only `ApiClient`/`ApiException` (still valid):**
- `ErrorClassifier.java` — imports `ApiException` only ✓
- `ExampleContext.java` — imports `DefaultApi`, `ApiClient` (DefaultApi broken)
- `TestExecutor.java` — imports `DefaultApi`, `ApiClient` (DefaultApi broken)

---

## CRITICAL ISSUE #2: `ProviderList200ResponseAllInner` class no longer exists

### Impact: `ProviderExample.java` fails to compile

`ProviderExample.java` line 8 imports:
```java
import opencode.sdk.model.ProviderList200ResponseAllInner;
```

This class does **not** exist in the generated SDK. The `ProviderList200Response.getAll()` now returns `List<Provider>` directly (not `List<ProviderList200ResponseAllInner>`).

**Additionally**, the code calls methods on this type that don't exist on `Provider`:
- `provider.getApi()` — `Provider` has NO `api` field
- `provider.getNpm()` — `Provider` has NO `npm` field
- `provider.getEnv()` — ✓ exists (returns `List<String>`)
- `provider.getModels()` — ✓ exists (returns `Map<String, Model>`)
- `provider.getId()` — ✓ exists
- `provider.getName()` — ✓ exists

---

## ISSUE #3: `fileStatus()` moved from old `DefaultApi` to `FileApi`

`FileOperationsExample.java` calls `api.fileStatus(directory, workspace)` which now returns `List<ModelFile>`. The `ModelFile` type is accessed using `.getStatus()`, `.getPath()`, `.getAdded()`, `.getRemoved()` — these need to be verified against the new `ModelFile` model.

---

## ISSUE #4: Method signature changes for `providerList()`

The old `DefaultApi.providerList()` returned `ProviderList200Response` with `.getAll()` returning a typed list. The new `ProviderApi.providerList()` still returns `ProviderList200Response` but `.getAll()` now returns `List<Provider>` (not `List<ProviderList200ResponseAllInner>`).

---

## ISSUE #5: `experimentalResourceList()` may have moved

Both `McpExample.java` and `ExperimentalExample.java` call `api.experimentalResourceList()`. This method needs to be verified as being on `ExperimentalApi` or another class.

---

## Detailed File-by-File Analysis

### 1. `Main.java` (159 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN: class doesn't exist
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
```
**SDK API calls:**
- `api.globalHealth()` → now in `GlobalApi`
- Creates `DefaultApi(apiClient)` → broken

---

### 2. `SystemInfoExample.java` (189 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.Agent             ← OK (exists)
opencode.sdk.model.AppSkills200ResponseInner ← OK (exists)
opencode.sdk.model.Command           ← OK (exists)
opencode.sdk.model.GlobalHealth200Response    ← OK (exists)
```
**SDK API calls (all on DefaultApi → all broken):**
- `api.globalHealth()` → `GlobalApi`
- `api.appAgents(null, null)` → `InstanceApi`
- `api.appSkills(null, null)` → `InstanceApi`
- `api.commandList(null, null)` → `InstanceApi`

---

### 3. `ConfigurationExample.java` (209 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.Config            ← OK
opencode.sdk.model.ConfigProviders200Response ← OK
opencode.sdk.model.LogLevel          ← OK
opencode.sdk.model.Provider          ← OK
```
**SDK API calls:**
- `api.configGet(null, null)` → `ConfigApi`
- `api.globalConfigGet()` → `GlobalApi`
- `api.configProviders(null, null)` → `ConfigApi`
- `api.configUpdate(null, null, configUpdate)` → `ConfigApi`

---

### 4. `ProviderExample.java` (167 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi                      ← BROKEN
opencode.sdk.invoker.ApiClient                   ← OK
opencode.sdk.invoker.ApiException                ← OK
opencode.sdk.model.ProviderAuthMethod            ← OK
opencode.sdk.model.ProviderList200Response       ← OK
opencode.sdk.model.ProviderList200ResponseAllInner ← BROKEN: class doesn't exist
```
**SDK API calls:**
- `api.providerList(null, null)` → `ProviderApi`
- `api.providerAuth(null, null)` → `ProviderApi`
- Calls `provider.getApi()` → BROKEN: Provider has no `api` field
- Calls `provider.getNpm()` → BROKEN: Provider has no `npm` field

---

### 5. `ProjectExample.java` (179 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.Project           ← OK
opencode.sdk.model.ProjectUpdateRequest ← OK
```
**SDK API calls:**
- `api.projectList(null, null)` → `ProjectApi`
- `api.projectCurrent(null, null)` → `ProjectApi`
- `api.projectUpdate(projectId, null, null, updateRequest)` → `ProjectApi`

---

### 6. `FileOperationsExample.java` (264 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.* (wildcard)      ← likely OK
```
**SDK API calls:**
- `api.fileList(path, null, null)` → `FileApi`
- `api.fileRead(path, null, null)` → `FileApi`
- `api.fileStatus(null, null)` → `FileApi`
- `api.findFiles(pattern, null, null, null, null, 10)` → `FileApi` (note: `10` is int, new API uses `Integer`)
- `api.findText(searchPattern, null, null)` → `FileApi`
- `api.findSymbols(query, null, null)` → `FileApi`

**Potential issue:** `api.findFiles(...)` last parameter: old code passes `int 10`, new API signature is `Integer` (nullable) — autoboxing should handle this.

---

### 7. `SessionCrudExample.java` (217 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.api.SessionApi          ← OK (class exists)
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.Session           ← OK
opencode.sdk.model.SessionCreateRequest ← OK
opencode.sdk.model.SessionUpdateRequest ← OK
```
**SDK API calls:**
- `api.sessionList(null, null, null, null, null, BigDecimal("10"))` → `SessionApi`
- `api.sessionCreate(null, null, request)` → `SessionApi`
- `sessionApi.sessionGet(sessionId, null, null)` → `SessionApi` (already uses SessionApi)
- `api.sessionUpdate(sessionId, null, null, request)` → `SessionApi`
- `api.sessionDelete(sessionId, null, null)` → `SessionApi`
- `api.globalHealth()` → `GlobalApi`

---

### 8. `SessionAdvancedExample.java` (288 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.api.SessionApi          ← OK
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.* (wildcard)      ← likely OK
```
**SDK API calls:**
- `api.sessionCreate(null, null, request)` → `SessionApi`
- `api.sessionFork(sessionId, null, null, request)` → `SessionApi`
- `sessionApi.sessionGet(sessionId, null, null)` → `SessionApi` (already correct)
- `sessionApi.sessionChildren(sessionId, null, null)` → `SessionApi` (already correct)
- `api.sessionShare(sessionId, null, null)` → `SessionApi`
- `api.sessionUnshare(sessionId, null, null)` → `SessionApi`
- `api.sessionSummarize(sessionId, null, null, request)` → `SessionApi`
- `api.sessionAbort(sessionId, null, null)` → `SessionApi`
- `api.sessionRevert(sessionId, null, null, request)` → `SessionApi`
- `api.sessionUnrevert(sessionId, null, null)` → `SessionApi`

**Note:** `sessionApi.sessionGet()` and `sessionApi.sessionChildren()` already use `SessionApi` correctly, so those calls would work if `SessionApi` were used throughout.

---

### 9. `MessageExample.java` (212 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.* (wildcard)      ← likely OK
```
**SDK API calls:**
- `api.sessionCreate(null, null, request)` → `SessionApi`
- `api.sessionPrompt(sessionId, null, null, request)` → `SessionApi`
- `api.sessionMessages(sessionId, null, null, BigDecimal("20"))` → `SessionApi`
- `api.globalHealth()` → `GlobalApi`

**Model access patterns:**
- `SessionPrompt200Response.getParts()` → returns `List<Part>`
- `Part.getActualInstance()` → returns `Object`, checked with `instanceof TextPart`
- `TextPart.getText()` → `String`
- `SessionMessages200ResponseInner.getInfo()` → `Message`
- `SessionMessages200ResponseInner.getParts()` → `List<Part>`

---

### 10. `DevToolsExample.java` (130 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.FormatterStatus   ← OK
opencode.sdk.model.LSPStatus         ← OK
```
**SDK API calls:**
- `api.lspStatus(null, null)` → `InstanceApi`
- `api.formatterStatus(null, null)` → `InstanceApi`

---

### 11. `ExperimentalExample.java` (179 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi                       ← BROKEN
opencode.sdk.invoker.ApiClient                    ← OK
opencode.sdk.invoker.ApiException                 ← OK
opencode.sdk.model.ExperimentalWorkspaceCreateRequest ← OK
opencode.sdk.model.GlobalSession                  ← OK
opencode.sdk.model.McpResource                    ← OK
opencode.sdk.model.Workspace                      ← OK
```
**SDK API calls:**
- `api.experimentalSessionList(null, null, null, null, null, null, BigDecimal("10"), null)` → `ExperimentalApi`
- `api.experimentalWorkspaceList(null, null)` → `WorkspaceApi`
- `api.experimentalWorkspaceCreate(null, null, request)` → `WorkspaceApi`
- `api.experimentalResourceList(null, null)` → `ExperimentalApi` (needs verification)

---

### 12. `InstanceExample.java` (129 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
```
**SDK API calls:** None (only explains APIs, doesn't actually call them)

---

### 13. `InteractiveExample.java` (231 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.* (wildcard)      ← likely OK
```
**SDK API calls:**
- `api.toolIds(null, null)` → needs verification (may be in `InstanceApi` or a tools API)
- `api.toolList("zai", "glm-4.7", null, null)` → needs verification
- `api.questionList(null, null)` → `QuestionApi`
- `api.questionReply(questionId, null, null, replyRequest)` → `QuestionApi`
- `api.questionReject(questionId, null, null)` → `QuestionApi`
- `api.permissionList(null, null)` → `PermissionApi`
- `api.permissionReply(permissionId, null, null, replyRequest)` → `PermissionApi`

**Model access:**
- `ToolListItem.getId()`, `ToolListItem.getDescription()` → needs verification
- `QuestionRequest.getId()`, `.getSessionID()`, `.getQuestions()`, `.getTool()` → needs verification
- `QuestionReplyRequest.setAnswers(List<List<String>>)` → needs verification
- `PermissionRequest.getId()`, `.getSessionID()`, `.getPermission()`, `.getPatterns()`, `.getTool()` → needs verification
- `PermissionReplyRequest.setReply(ReplyEnum.ONCE)` → needs verification

---

### 14. `McpExample.java` (214 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.* (wildcard)      ← likely OK
```
**SDK API calls:**
- `api.mcpStatus(null, null)` → `McpApi`
- `api.mcpAdd(null, null, request)` → `McpApi`
- `api.mcpConnect(name, null, null)` → `McpApi`
- `api.experimentalResourceList(null, null)` → `ExperimentalApi` or `McpApi`
- `api.mcpAuthStart(name, null, null)` → `McpApi`

**Model access:**
- `MCPStatus.getActualInstance()` → polymorphic, needs verification
- `McpLocalConfig.setType(TypeEnum.LOCAL)`, `.setCommand(...)`, `.setEnabled(...)`, `.setTimeout(...)` → needs verification
- `McpAddRequestConfig(McpLocalConfig)` → constructor wrapping, needs verification
- `McpAddRequest.setName(...)`, `.setConfig(...)` → needs verification
- `McpResource.getName()`, `.getUri()`, `.getDescription()`, `.getMimeType()`, `.getClient()` → needs verification
- `McpAuthStart200Response.getAuthorizationUrl()` → needs verification

---

### 15. `TodoExample.java` (120 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.Session           ← OK
opencode.sdk.model.Todo              ← OK
```
**SDK API calls:**
- `api.sessionList(null, null, null, null, null, null)` → `SessionApi`
- `api.sessionTodo(sessionId, null, null)` → `SessionApi`

---

### 16. `VcsExample.java` (179 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.VcsInfo           ← OK
opencode.sdk.model.Worktree           ← OK
opencode.sdk.model.WorktreeCreateInput  ← OK
opencode.sdk.model.WorktreeRemoveInput ← OK
```
**SDK API calls:**
- `api.vcsGet(null, null)` → `InstanceApi`
- `api.worktreeList(null, null)` → needs verification (may be in `InstanceApi`)
- `api.worktreeCreate(null, null, input)` → needs verification
- `api.worktreeRemove(null, null, input)` → needs verification

---

### 17. `EventStreamingExample.java` (129 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.Event             ← OK
opencode.sdk.model.GlobalEvent       ← OK
```
**SDK API calls:**
- `api.eventSubscribe(null, null)` → `EventApi`
- `api.globalEvent()` → `GlobalApi`

**Model access:**
- `GlobalEvent.getDirectory()` → needs verification
- `GlobalEvent.getPayload()` → needs verification (may be `GlobalEventPayload` type)
- `Event.toString()` → OK

---

### 18. `PtyExample.java` (237 lines)
**Imports:**
```java
opencode.sdk.api.DefaultApi          ← BROKEN
opencode.sdk.invoker.ApiClient       ← OK
opencode.sdk.invoker.ApiException    ← OK
opencode.sdk.model.Pty               ← OK
opencode.sdk.model.PtyCreateRequest  ← OK
opencode.sdk.model.PtyUpdateRequest  ← OK
opencode.sdk.model.PtyUpdateRequestSize ← OK
```
**SDK API calls:**
- `api.ptyList(null, null)` → `PtyApi`
- `api.ptyCreate(null, null, request)` → `PtyApi`
- `api.ptyGet(ptyId, null, null)` → `PtyApi`
- `api.ptyUpdate(ptyId, null, null, request)` → `PtyApi`
- `api.ptyRemove(ptyId, null, null)` → `PtyApi`

---

### Testing Utility Files

**`ExampleContext.java`** (43 lines)
- Imports `DefaultApi`, `ApiClient` → `DefaultApi` BROKEN
- Stores `DefaultApi defaultApi` field → needs migration

**`TestExecutor.java`** (141 lines)
- Imports `DefaultApi`, `ApiClient` → `DefaultApi` BROKEN
- Creates `new DefaultApi(apiClient)` → BROKEN
- Creates `new CleanupManager(defaultApi, testLogger)` → depends on DefaultApi fix

**`CleanupManager.java`** (87 lines)
- Imports `DefaultApi` → BROKEN
- Calls `api.sessionDelete(sessionId, null, null)` → method now on `SessionApi`

**`ErrorClassifier.java`** (84 lines)
- Imports only `ApiException` → OK ✓

**Other testing files** (no SDK imports):
- `ArgumentParser.java`, `CleanupResult.java`, `EnvironmentLoader.java`, `ExampleRegistry.java`, `ExampleWrapper.java`, `ResourceTracker.java`, `ResponseValidator.java`, `ResultReporter.java`, `SensitiveDataMasker.java`, `TestConfiguration.java`, `TestLogger.java`, `TestResult.java`, `TestResults.java`, `TestRunner.java`, `TrackedResource.java`, `ValidationResult.java`
- These files only use `opencode.examples.plainjava.*` or `opencode.examples.plainjava.testing.*` imports and standard Java libraries → OK ✓

---

## Consolidated List of Broken Imports/References

| Broken Import | Files Affected | Count |
|---|---|---|
| `opencode.sdk.api.DefaultApi` | All 20 example files + `ExampleContext`, `TestExecutor`, `CleanupManager` | **23** |
| `opencode.sdk.model.ProviderList200ResponseAllInner` | `ProviderExample.java` | **1** |

## Consolidated List of Broken Method Calls (via DefaultApi)

Every `api.methodName(...)` call in all 20 example files is broken because `api` is typed as `DefaultApi` which doesn't exist. These calls need to be migrated to the appropriate new API class.

## Additional Model Field Issues

| Broken Call | File | Issue |
|---|---|---|
| `provider.getApi()` | `ProviderExample.java:82` | `Provider` model has no `api` field |
| `provider.getNpm()` | `ProviderExample.java:84` | `Provider` model has no `npm` field |

## Migration Strategy Recommendations

1. **Replace `DefaultApi` with multiple API instances**: Each example file needs to instantiate multiple API classes (e.g., `new GlobalApi(apiClient)`, `new SessionApi(apiClient)`, etc.)

2. **Update `ExampleContext`**: Instead of storing a single `DefaultApi`, store all needed API instances or provide factory methods

3. **Fix `ProviderExample`**: Remove `ProviderList200ResponseAllInner` import and usage; use `Provider` directly from `ProviderList200Response.getAll()`. Remove calls to `getApi()` and `getNpm()`

4. **Fix `CleanupManager`**: Change from `DefaultApi` to `SessionApi` for `sessionDelete()` call

5. **Method signatures are largely unchanged**: Most method signatures on the new individual API classes are the same as they were on the old `DefaultApi`, just distributed across different classes. The main work is updating imports and changing which API instance to call methods on

6. **Verify polymorphic model access**: Some model classes like `MCPStatus`, `Part`, `ProviderList200Response` use polymorphism (`getActualInstance()`, oneOf/anyOf patterns). These patterns should be verified against the new generated model code
