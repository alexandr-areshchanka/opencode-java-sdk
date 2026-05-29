# Fix Compile Issues After SDK Migration — Execution Plan

## Goal
Fix all compile issues in the examples modules (`plain-java` and `spring-boot`) after the OpenCode SDK migration, which replaced the single `DefaultApi` class with 22 tag-specific API classes.

## Scope
- **In scope**: Spring Boot Starter (`opencode-spring-boot-starter`), Plain Java Examples (`examples/plain-java`), Spring Boot Examples (`examples/spring-boot`)
- **Out of scope**: SDK module itself (auto-generated, already correct), documentation updates (`AGENTS.md`)

## Specification

### Requirements
- R1. Starter must expose all 22 new API classes via typed accessor methods (replacing the old `DefaultApi` facade)
- R2. All 20 plain-java example files + 3 testing utility files must compile without errors
- R3. All 16 spring-boot controller files must compile without errors
- R4. Specific model/method fixes:
  - Remove `ProviderList200ResponseAllInner` usage; `ProviderList200Response.getAll()` now returns `List<Provider>`
  - Remove calls to `Provider.getApi()` and `Provider.getNpm()` (fields no longer exist)
  - Update `sessionList` calls to match 8-parameter signature: `(directory, workspace, scope, path, roots, start, search, limit)`
  - Update `sessionMessages` calls to match 5-parameter signature: `(sessionID, directory, workspace, limit, before)`
  - Update `sessionShell` return type from `AssistantMessage` to `SessionShell200Response`

### Non-Goals
- NG1. Do not update documentation or `AGENTS.md`
- NG2. Do not change SDK generated code

### Acceptance Scenarios
- S1. `mvn compile -pl opencode-spring-boot-starter` succeeds
- S2. `mvn compile -pl examples/plain-java -am` succeeds
- S3. `mvn compile -pl examples/spring-boot -am` succeeds

## How to Use This Plan
1. Open the next unchecked task from the checklist below.
2. Read the corresponding task file completely.
3. Use the suggested agent and the provided inputs for that task.
4. Execute only the next unchecked task unless the user changes the plan.
5. Verify all acceptance criteria, including the git commit requirement.
6. Update the checklist after the task is completed.
7. If the plan becomes stale, update the relevant files before continuing.

## Task Checklist
- [ ] `task-01-starter-update.md`: Update Spring Boot Starter — Suggested agent: Code — Covers: R1, S1
- [ ] `task-02-plain-java-examples.md`: Fix Plain Java Examples — Suggested agent: Code — Covers: R2, R4, S2
- [ ] `task-03-spring-boot-examples.md`: Fix Spring Boot Examples — Suggested agent: Code — Covers: R3, R4, S3
- [ ] `task-04-verify-compilation.md`: Verify Compilation — Suggested agent: Code — Covers: S1, S2, S3

## Shared Context

### Key Decisions
- The starter will use the factory method pattern: typed accessor methods like `configApi()`, `sessionApi()`, `globalApi()` etc. — following the existing `sessionApi()` precedent. Each method creates an API instance on-the-fly from the stored `ApiClient`, which is the same pattern used by the generated API constructors.
- The starter will NOT store pre-created API instances as fields — they're lightweight objects that copy references from `ApiClient`, so on-the-fly creation is fine and avoids eager initialization issues.
- The old `OpenCodeService.api()` method returning `DefaultApi` will be removed entirely.
- `OpenCodeService.getHealth()` will be updated to use `GlobalApi`.

### Constraints
- No Lombok in starter (project convention)
- No inner classes — separate files (project convention)
- Follow existing code patterns (explicit getters/setters)
- All 22 API classes must be accessible via the starter

### Risks / Open Questions
- Some model field accesses (e.g., `MCPStatus.getActualInstance()`, `Part.getActualInstance()`) use polymorphic patterns that may have changed — need runtime verification
- The `Provider` model lost `api`/`npm` fields — the example code using these needs adaptation (comment out or adapt the logic)

## Research Artifacts
- `.tasks/migration-review-examples/research/sdk-api-surface.md` — Complete list of all 22 API classes, their methods, parameters, and return types
- `.tasks/migration-review-examples/research/plain-java-findings.md` — File-by-file analysis of all 38 plain-java example files with broken imports/references
- `.tasks/migration-review-examples/research/spring-boot-findings.md` — File-by-file analysis of all 17 spring-boot example files with compile issues
- `.tasks/migration-review-examples/research/starter-findings.md` — Complete current source of all 3 starter files with broken references marked
