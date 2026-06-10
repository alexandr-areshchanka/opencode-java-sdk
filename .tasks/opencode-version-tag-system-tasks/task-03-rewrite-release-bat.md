# Task 03: Rewrite `release.bat`

**Type:** Code Modification

## Goal

Windows equivalent of Task 02 — same release flow in Windows batch script syntax, with full e2e test gate.

## What to Do

- Mirror all functionality from Task 02's `release.sh` in Windows batch syntax
- Read target version from `.opencode-version` file at project root
- Rebuild Docker image with `OPENCODE_VERSION` build arg
- Wait for server health check
- Verify installed OpenCode version matches `.opencode-version`
- Strip `-SNAPSHOT` from all pom.xml `<revision>` properties
- Build full SDK: `mvn clean install`
- **Run integration tests**: `mvn verify -PrunIntegrationTests -pl examples/spring-boot -am` — **abort on failure**
- Git commit clean version, create tag `v{version}`
- Bump `.opencode-version` to next patch, update pom.xml to `{nextVersion}-SNAPSHOT`
- Git commit SNAPSHOT bump
- Print release summary

## Files/Areas

- `release.bat` — Complete rewrite (replace existing 343-line script)

## Key Points

- Must match `release.sh` feature-for-feature — same steps, same abort conditions, same output
- Use PowerShell-compatible commands where batch is insufficient (e.g., version string manipulation, JSON parsing)
- Keep the existing `release.bat` structural patterns: `goto`-based functions, `setlocal enabledelayedexpansion`, `call :log_info` style
- Docker commands are cross-platform — main differences are: string manipulation (use PowerShell snippets or batch string ops), `sed` equivalents (use PowerShell `(-replace)`), and `grep` equivalents (use `findstr` or PowerShell `Select-String`)
- For reading `.opencode-version`: `set /p OPENCODE_VERSION=<.opencode-version`
- For next version calculation (patch bump): use PowerShell one-liner for arithmetic on version parts
- Accept optional version argument: `release.bat [version]`
- Docker compose is run from `docker/opencode/` directory
- Container name: `opencode-server`
- `.env.opencode` must exist in `docker/opencode/`

## Done When

- [ ] `release.bat` reads version from `.opencode-version`
- [ ] Docker builds with pinned version via build arg
- [ ] Script verifies installed version matches target
- [ ] Integration tests run and release aborts on failure
- [ ] Git tag `v{version}` created on clean version commit
- [ ] SNAPSHOT bump committed after tag
- [ ] Feature parity with `release.sh` (same steps, same abort conditions)
- [ ] Script accepts optional version argument to override `.opencode-version`
