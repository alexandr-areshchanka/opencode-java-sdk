# Task 01: Update Spring Boot Starter

**Type:** Code Modification
**Suggested agent:** Code

## Goal
Replace the `DefaultApi`-based starter with typed accessor methods for all 22 new SDK API classes, so that downstream consumers (spring-boot examples) can call `openCodeService.xxxApi()` for any API class.

## Why This Task Exists
The SDK migration replaced the single `DefaultApi` with 22 tag-specific API classes. The starter currently stores and exposes `DefaultApi`, which no longer exists. This is a blocking dependency for Tasks 02 and 03.

## Spec Coverage
- Requirements: R1
- Scenarios: S1

## Required Inputs
- `.tasks/migration-review-examples/research/starter-findings.md` — Complete current source code of all 3 starter files
- `.tasks/migration-review-examples/research/sdk-api-surface.md` — All 22 API class names and their constructor patterns

## Files/Areas
- `opencode-spring-boot-starter/src/main/java/opencode/sdk/springboot/autoconfigure/OpenCodeAutoConfiguration.java` — Remove `DefaultApi` bean, update `OpenCodeService` bean creation
- `opencode-spring-boot-starter/src/main/java/opencode/sdk/springboot/OpenCodeService.java` — Replace `DefaultApi` field with typed accessor methods for all 22 API classes
- `opencode-spring-boot-starter/src/main/java/opencode/sdk/springboot/autoconfigure/OpenCodeProperties.java` — No changes needed

## Constraints / Non-Goals
- No Lombok (project convention for starter)
- No inner classes
- Follow the existing `sessionApi()` factory method pattern
- Do NOT store all 22 API instances as fields — use lazy factory methods
- `apiClient` bean remains unchanged
- `OpenCodeProperties` remains unchanged
- Auto-configuration registration file remains unchanged

## Output Artifacts
- N/A — direct file edits

## What to Do

### 1. Update `OpenCodeService.java`
- Remove the `DefaultApi defaultApi` field
- Remove the constructor parameter `DefaultApi defaultApi` — keep only `ApiClient apiClient`
- Remove the `api()` method (returned `DefaultApi`)
- Update `getHealth()` to create `new GlobalApi(apiClient).globalHealth()` instead of `defaultApi.globalHealth()`
- Keep `sessionApi()` as is (already correct pattern)
- Add accessor methods for ALL 22 API classes following the `sessionApi()` pattern:
  ```java
  public ConfigApi configApi() { return new ConfigApi(apiClient); }
  public ControlApi controlApi() { return new ControlApi(apiClient); }
  public EventApi eventApi() { return new EventApi(apiClient); }
  public ExperimentalApi experimentalApi() { return new ExperimentalApi(apiClient); }
  public FileApi fileApi() { return new FileApi(apiClient); }
  public GlobalApi globalApi() { return new GlobalApi(apiClient); }
  public InstanceApi instanceApi() { return new InstanceApi(apiClient); }
  public McpApi mcpApi() { return new McpApi(apiClient); }
  public PermissionApi permissionApi() { return new PermissionApi(apiClient); }
  public ProjectApi projectApi() { return new ProjectApi(apiClient); }
  public ProviderApi providerApi() { return new ProviderApi(apiClient); }
  public PtyApi ptyApi() { return new PtyApi(apiClient); }
  public PtyWsApi ptyWsApi() { return new PtyWsApi(apiClient); }
  public QuestionApi questionApi() { return new QuestionApi(apiClient); }
  public SessionApi sessionApi() { return new SessionApi(apiClient); }
  public SyncApi syncApi() { return new SyncApi(apiClient); }
  public TuiApi tuiApi() { return new TuiApi(apiClient); }
  public V2Api v2Api() { return new V2Api(apiClient); }
  public V2MessagesApi v2MessagesApi() { return new V2MessagesApi(apiClient); }
  public V2ModelsApi v2ModelsApi() { return new V2ModelsApi(apiClient); }
  public V2ProvidersApi v2ProvidersApi() { return new V2ProvidersApi(apiClient); }
  public WorkspaceApi workspaceApi() { return new WorkspaceApi(apiClient); }
  ```
- Import all 22 API classes from `opencode.sdk.api.*`

### 2. Update `OpenCodeAutoConfiguration.java`
- Remove the `import opencode.sdk.api.DefaultApi`
- Remove the `defaultApi()` bean method entirely
- Update `openCodeService()` bean: remove `DefaultApi defaultApi` parameter, pass only `ApiClient apiClient`:
  ```java
  @Bean
  @ConditionalOnMissingBean
  public opencode.sdk.springboot.OpenCodeService openCodeService(ApiClient apiClient) {
      return new opencode.sdk.springboot.OpenCodeService(apiClient);
  }
  ```

## Expected Output
- `OpenCodeService.java` — compiles with all 22 API accessor methods, no `DefaultApi` references
- `OpenCodeAutoConfiguration.java` — compiles with only `ApiClient` and `OpenCodeService` beans

## Acceptance Criteria
- [ ] No import of `opencode.sdk.api.DefaultApi` in any starter file
- [ ] All 22 API classes have accessor methods in `OpenCodeService`
- [ ] `getHealth()` uses `GlobalApi` instead of `DefaultApi`
- [ ] `OpenCodeAutoConfiguration` creates `ApiClient` and `OpenCodeService` beans only
- [ ] `mvn compile -pl opencode-spring-boot-starter` succeeds
- [ ] Covered requirements and scenarios are satisfied
- [ ] I've created a git commit for this task
