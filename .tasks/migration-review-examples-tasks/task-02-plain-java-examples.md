# Task 02: Fix Plain Java Examples

**Type:** Code Modification
**Suggested agent:** Code

## Goal
Update all 20 plain-java example files + 3 testing utility files to use the correct API classes instead of `DefaultApi`, and fix all model-related compile errors.

## Why This Task Exists
All 23 files in `examples/plain-java/` import `opencode.sdk.api.DefaultApi` which no longer exists. Each file must be updated to instantiate the correct API class(es) for its specific endpoint calls.

## Spec Coverage
- Requirements: R2, R4
- Scenarios: S2

## Required Inputs
- `.tasks/migration-review-examples/research/plain-java-findings.md` — File-by-file analysis with exact method calls, imports, and issues for all 38 files
- `.tasks/migration-review-examples/research/sdk-api-surface.md` — All 22 API classes with exact method signatures, parameter types, and return types

## Files/Areas

### Example files (all in `examples/plain-java/src/main/java/opencode/examples/plainjava/`):
- `Main.java` — Uses `GlobalApi.globalHealth()`
- `SystemInfoExample.java` — Uses `GlobalApi`, `InstanceApi`
- `ConfigurationExample.java` — Uses `ConfigApi`, `GlobalApi`
- `ProviderExample.java` — Uses `ProviderApi`; has broken model references (ProviderList200ResponseAllInner, getApi(), getNpm())
- `ProjectExample.java` — Uses `ProjectApi`
- `FileOperationsExample.java` — Uses `FileApi`
- `SessionCrudExample.java` — Uses `SessionApi`, `GlobalApi`; `sessionList` has 8 params (currently passes 6 with nulls, needs 2 more nulls for `search` and `limit`)
- `SessionAdvancedExample.java` — Uses `SessionApi`
- `MessageExample.java` — Uses `SessionApi`, `GlobalApi`; `sessionMessages` has 5 params (currently passes 4, needs `before` param added)
- `DevToolsExample.java` — Uses `InstanceApi`
- `ExperimentalExample.java` — Uses `ExperimentalApi`, `WorkspaceApi`
- `InstanceExample.java` — Uses various APIs (mostly documentation, may not call methods)
- `InteractiveExample.java` — Uses `InstanceApi` (toolIds, toolList), `QuestionApi`, `PermissionApi`
- `McpExample.java` — Uses `McpApi`, `ExperimentalApi`
- `TodoExample.java` — Uses `SessionApi`; `sessionList` has 8 params
- `VcsExample.java` — Uses `InstanceApi`, `ExperimentalApi`
- `EventStreamingExample.java` — Uses `EventApi`, `GlobalApi`
- `PtyExample.java` — Uses `PtyApi`

### Testing utility files (all in `examples/plain-java/src/main/java/opencode/examples/plainjava/testing/`):
- `ExampleContext.java` — Stores `DefaultApi` field, needs multiple API class fields
- `TestExecutor.java` — Creates `DefaultApi`, needs to create appropriate API instances
- `CleanupManager.java` — Stores `DefaultApi` field for `sessionDelete()`, needs `SessionApi`

## Constraints / Non-Goals
- No Lombok (plain-java module convention)
- Method signatures on the new API classes are identical to the old DefaultApi — only the class name changes
- Do NOT change the logic/behavior of examples, only fix compile issues
- For `ProviderExample.java`: if `Provider` no longer has `api`/`npm` fields, comment out or remove the code that accesses them with a TODO comment explaining the field was removed

## Output Artifacts
- N/A — direct file edits

## What to Do

### General Pattern for Each Example File
1. Replace `import opencode.sdk.api.DefaultApi` with imports for the specific API classes needed
2. Replace `DefaultApi api = new DefaultApi(apiClient)` with individual API instances: `GlobalApi globalApi = new GlobalApi(apiClient)`, etc.
3. Replace `api.methodName(...)` calls with the correct API instance: `globalApi.globalHealth()`, `sessionApi.sessionList(...)`, etc.
4. Verify parameter counts match the new signatures

### Specific Fixes

#### `ProviderExample.java`
- Remove `import opencode.sdk.model.ProviderList200ResponseAllInner`
- Change `api` type from `DefaultApi` to `ProviderApi`
- For `response.getAll()` — this now returns `List<Provider>` directly (no more `ProviderList200ResponseAllInner`)
- Remove or comment out code accessing `provider.getApi()` and `provider.getNpm()` with a TODO comment

#### `SessionCrudExample.java` and `TodoExample.java`
- `sessionList` now takes 8 params: `(directory, workspace, scope, path, roots, start, search, limit)`
- Old call: `api.sessionList(null, null, null, null, null, BigDecimal("10"))` — 6 args
- Fix: Add `null` for `search` and adjust — `sessionApi.sessionList(null, null, null, null, null, null, null, new BigDecimal("10"))` — 8 args

#### `MessageExample.java`
- `sessionMessages` now takes 5 params: `(sessionID, directory, workspace, limit, before)`
- Old call: `api.sessionMessages(sessionId, null, null, BigDecimal("20"))` — 4 args
- Fix: `sessionApi.sessionMessages(sessionId, null, null, new BigDecimal("20"), null)` — 5 args

#### `ExampleContext.java`
- Replace `DefaultApi defaultApi` field with `ApiClient apiClient` field
- Add factory methods or individual fields for needed API classes
- Keep backward compatibility for consumers

#### `TestExecutor.java`
- Replace `new DefaultApi(apiClient)` with `ApiClient` storage
- Update `ExampleContext` creation to use new constructor

#### `CleanupManager.java`
- Replace `DefaultApi api` with `SessionApi sessionApi`
- Update `api.sessionDelete(...)` to `sessionApi.sessionDelete(...)`

## Expected Output
- All 23 files compile without errors
- No references to `DefaultApi` or `ProviderList200ResponseAllInner` remain
- All method calls use the correct API class with correct parameter counts

## Acceptance Criteria
- [ ] Zero imports of `opencode.sdk.api.DefaultApi` in any file
- [ ] Zero imports of `ProviderList200ResponseAllInner`
- [ ] All `sessionList` calls have exactly 8 parameters
- [ ] All `sessionMessages` calls have exactly 5 parameters
- [ ] `ProviderExample` has no calls to `getApi()` or `getNpm()` on Provider
- [ ] `mvn compile -pl examples/plain-java -am` succeeds
- [ ] Covered requirements and scenarios are satisfied
- [ ] I've created a git commit for this task
