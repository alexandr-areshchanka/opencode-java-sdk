# Task 02: Rewrite `release.sh`

**Type:** Code Modification

## Goal

Replace the Linux/macOS release script with a version-aware script that reads `.opencode-version`, pins Docker build, runs e2e tests, and manages git tags.

## What to Do

- Read target version from `.opencode-version` file at project root
- Rebuild Docker image with `OPENCODE_VERSION` build arg set to the target version
- Wait for server health check (reuse existing health check pattern)
- Verify the installed OpenCode version inside the container matches `.opencode-version` (abort on mismatch)
- Strip `-SNAPSHOT` from all pom.xml `<revision>` properties (root pom.xml + `examples/spring-boot/pom.xml`)
- Build full SDK: `mvn clean install`
- **Run integration tests**: `mvn verify -PrunIntegrationTests -pl examples/spring-boot -am` — **abort release on test failure**
- Git commit the clean version changes
- Create git tag `v{version}` (e.g., `v1.17.1`)
- Bump `.opencode-version` to next patch version (e.g., `1.17.1` → `1.17.2`)
- Update all pom.xml `<revision>` to `{nextVersion}-SNAPSHOT`
- Git commit the SNAPSHOT bump
- Print release summary

## Files/Areas

- `release.sh` — Complete rewrite (replace existing 272-line script)

## Key Points

- The script must be idempotent — safe to re-run if it fails mid-way (check for existing tags, skip already-done steps)
- **Test failure = hard stop** — no tag created, no version bump. Roll back pom.xml changes on failure.
- Version bump is always patch (e.g., `1.17.1` → `1.17.2`). Do NOT auto-bump minor or major.
- Follow existing script's logging patterns (colored output with `log_info`, `log_success`, `log_warning`, `log_error`)
- The existing script extracts version from the container's `package.json` via `docker exec` — keep that technique for the verification step
- Must handle both macOS (BSD `sed -i ''`) and Linux (GNU `sed -i`) for in-place edits — use the existing pattern from the current script
- The script should accept an optional version argument: `./release.sh [version]` — if provided, update `.opencode-version` first
- Docker compose file is at `docker/opencode/docker-compose.yml` — pass `OPENCODE_VERSION` as build arg
- Current `docker compose build` is run from `docker/opencode/` directory
- Existing container name is `opencode-server`
- Existing config: `.env.opencode` file must exist in `docker/opencode/`

## Done When

- [ ] `release.sh` reads version from `.opencode-version`
- [ ] Docker builds with pinned version via build arg
- [ ] Script verifies installed version matches target
- [ ] Integration tests run as part of release pipeline
- [ ] Release aborts on test failure without creating tags or bumping versions
- [ ] Git tag `v{version}` created on clean version commit
- [ ] SNAPSHOT bump committed after tag
- [ ] Script handles both macOS and Linux
- [ ] Script accepts optional version argument to override `.opencode-version`
