# OpenCode Version Tag System — Task Execution Plan

## Your Mission

Implement a version-aware tag system that pins the OpenCode Docker container to an exact release version, keeps the Java SDK pom.xml in sync, gates releases behind passing integration tests, and manages git tags for each release. The `.opencode-version` file is the single source of truth.

**Plan File:** `.tasks/opencode-version-tag-system-tasks/PLAN.md`
**Tasks Directory:** `.tasks/opencode-version-tag-system-tasks/`

## Execution Steps

### 1. Read This Plan
Review this file for the next incomplete task, key decisions, and information from previous agents.

### 2. Understand Your Task
Read your task file: `.tasks/opencode-version-tag-system-tasks/task-XX-[name].md`
- **Goal** — What you are trying to achieve
- **Key Points** — Important considerations
- **Done When** — Objective acceptance criteria

### 3. Execute the Task
- Make necessary code changes
- Ensure code compiles without errors
- Verify all Done When criteria are met

### 4. Update This Plan
- Mark the task as completed in `## Task Plan`
- Add a 1-2 sentence outcome summary in `## Shared Context`
- Document only critical decisions that affect future tasks

### 5. Await Approval (MANDATORY)
Wait for user confirmation before proceeding to the next task.

### 6. Review Task List (MANDATORY)
Analyze remaining tasks based on what you learned:
- Did you encounter unexpected complexity?
- Should any tasks be split, merged, removed, or reordered?
- Are there missing tasks?

### 7. Present Review Findings (MANDATORY)
Always present your findings — even if no changes are needed — and await user approval before proceeding.

### 8. Update Task Files (if approved)
- Modify/create task files as needed
- Update `## Task Plan` in PLAN.md accordingly

---

## Task Plan

- [x] task-01-version-file-and-docker-config.md: Create `.opencode-version` and update Docker config
- [x] task-02-rewrite-release-sh.md: Rewrite `release.sh`
- [x] task-03-rewrite-release-bat.md: Rewrite `release.bat`
- [x] task-04-update-documentation.md: Update project documentation
- [x] task-05-verify-end-to-end.md: Verify end-to-end

---

## Shared Context

### Overview
Implement a version tag system where `.opencode-version` is the single source of truth. Docker builds pin to the exact OpenCode version. Release scripts build Docker, verify version, run integration tests, tag git, and bump SNAPSHOT. No release proceeds without passing tests.

### Project Context
- `.opencode-version` — Single line semver string (no `v` prefix), e.g., `1.17.1`. **Source of truth.**
- `pom.xml` — Root POM uses `<revision>1.17.1-SNAPSHOT</revision>` property. During release, SNAPSHOT is stripped, then bumped to next patch.
- `examples/spring-boot/pom.xml` — Also uses its own `<revision>` property, must be updated in sync.
- `docker/opencode/Dockerfile` — Currently `FROM node:24-alpine`, installs via `curl -fsSL https://opencode.ai/install | bash`. Install script supports `--version` flag.
- `docker/opencode/docker-compose.yml` — Builds from Dockerfile, runs container `opencode-server` on port 4096.
- `examples/spring-boot/.../OpenCodeServerContainer.java` — Testcontainers support class, currently uses image `opencode-server:test`. Must be versioned.
- `examples/spring-boot/pom.xml` — Has `build-docker-image` Maven profile that builds Docker image for tests. Has `integration-tests` profile (activated by `runIntegrationTests` property) that runs `*IT.java` tests.
- 12 integration test classes in `examples/spring-boot/src/test/java/.../controller/` — Test all SDK endpoints via Testcontainers.
- `release.sh` / `release.bat` — Current scripts: build Docker → health check → extract version from container → update pom.xml → build SDK. **Being replaced.**

### Key Decisions
- **`.opencode-version` is the single source of truth** — Docker, pom.xml, and release scripts all read from it.
- **Version pinning via install script `--version` flag** — `curl -fsSL https://opencode.ai/install | bash -s -- --version 1.17.1`. Also supports `VERSION` env var.
- **Release flow is inverted** — version is declared upfront (not extracted from container after build). Docker must match the declared version.
- **Integration tests are a mandatory gate** — release scripts abort on test failure. No tag, no bump.
- **SNAPSHOT management** — Between releases, pom.xml uses `{version}-SNAPSHOT`. Release script strips SNAPSHOT, builds, tags, then bumps to next patch SNAPSHOT.
- **Git tags use `v` prefix** — e.g., `v1.17.1`.
- **Patch-only bump** — Release script bumps patch only (1.17.1 → 1.17.2). Minor/major bumps are manual.
- **Existing release scripts are replaced** — No old/new coexistence.
- **No auto-push** — Scripts create tags and commits locally. User reviews and pushes manually.

### Task 05 Outcome
End-to-end review found 3 bugs and 4 warnings. All fixed: (1) Maven profile activation fixed to use `-Pintegration-tests`, (2) Docker image naming mismatch fixed with `docker tag` alias, (3) `grep -oP` replaced with cross-platform `grep -oE`+`sed`, (4) removed hardcoded default in docker-compose.yml, (5) release scripts now update `<opencode.version>` property, (6) Docker README `:latest` replaced with versioned tag, (7) ANSI ESC character fixed in release.bat logging.

### Task 04 Outcome
Updated README.md, AGENTS.md, docker/opencode/README.md, and docker/opencode/AGENTS.md with `.opencode-version` documentation, version-aware release flow, Docker build arg docs, and integration test gate documentation.

### Task 03 Outcome
Rewrote `release.bat` (343→575 lines). Full feature parity with `release.sh` — all 14 steps, PowerShell helpers, colored logging, idempotent git ops, rollback on failure.

### Task 02 Outcome
Rewrote `release.sh` (272→482 lines). Implements 14-step release pipeline with colored logging, cross-platform sed, idempotent re-runs, test failure rollback, patch-only bump, and local-only git operations.

### Task 01 Outcome
Created `.opencode-version` with `1.17.1`. Dockerfile now pins install via `--version` flag and has a verification step. docker-compose.yml passes `OPENCODE_VERSION` build arg. OpenCodeServerContainer uses versioned image tag (`opencode-server:1.17.1`). Spring Boot pom.xml passes version to Docker build plugin. All compiles successfully.

### Current Versions (as of planning)
- OpenCode latest: **1.17.1**
- Project pom.xml: **1.15.12-SNAPSHOT** (4 versions behind)
- No git tags exist in the repository

### Caveats & Problems
- The OpenAPI spec download from `/doc` endpoint is currently disabled (commented out) in release scripts because the server returns an incomplete spec. This is not part of the current task scope.
- The `opencode` git submodule (`anomalyco/opencode.git`) is used for reference but not for version control — version comes from `.opencode-version` + install script.
- Integration tests require `Z_AI_API_KEY` environment variable for the OpenCode server to start.
- The `OpenCodeServerContainer` test class has a hardcoded fallback API key — this should be reviewed for security.
