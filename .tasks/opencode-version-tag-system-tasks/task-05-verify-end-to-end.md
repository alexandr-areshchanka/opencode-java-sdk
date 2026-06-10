# Task 05: Verify End-to-End

**Type:** Verification

## Goal

Validate the entire version-aware release pipeline works end-to-end — from `.opencode-version` through Docker build, version verification, SDK compilation, and integration tests.

## What to Do

- Verify `.opencode-version` exists and contains a valid semver string
- Build Docker image with pinned version: `cd docker/opencode && docker compose build`
- Confirm the correct OpenCode version is installed in the container:
  - Start the container, run `docker exec opencode-server opencode --version` (or check package.json)
  - Verify it matches `.opencode-version` exactly
- Verify Dockerfile version mismatch detection works:
  - Temporarily set a wrong version in `.opencode-version`, build, confirm it fails
  - Restore correct version
- Verify SDK compiles: `mvn clean install -pl sdk -am`
- Run integration tests: `mvn verify -PrunIntegrationTests -pl examples/spring-boot -am`
  - Requires Docker running and `Z_AI_API_KEY` environment variable set
- Test git tag flow (dry-run):
  - Run through release script steps manually or with a dry-run flag
  - Verify tag format is `v{version}` (e.g., `v1.17.1`)
  - Verify tag points to the correct commit
  - Clean up the dry-run tag after verification
- Verify SNAPSHOT bump logic:
  - After tag, confirm `.opencode-version` and pom.xml files are at `{nextPatchVersion}-SNAPSHOT`
  - Confirm the version arithmetic is correct (e.g., `1.17.1` → `1.17.2-SNAPSHOT`)

## Files/Areas

- No file changes — verification only
- `release.sh` and/or `release.bat` — exercise the scripts
- `.opencode-version` — read and verify
- Docker container — verify version

## Key Points

- This is a smoke test of the entire pipeline assembled by Tasks 01–04
- If any step fails, report the issue clearly so the relevant task can be fixed
- Integration tests require Docker running and `Z_AI_API_KEY` set (same as existing test setup)
- The existing 12 integration test classes (`*IT.java`) in `examples/spring-boot/src/test/` exercise all SDK endpoints via Testcontainers
- Do NOT push any tags to remote during verification — create and delete locally only
- The test container uses `opencode-server:{version}` image — verify it matches the Docker-built image

## Done When

- [ ] Docker build produces image with exact version from `.opencode-version`
- [ ] Version mismatch detection in Dockerfile works (fails on wrong version)
- [ ] SDK compiles successfully
- [ ] Integration tests pass against the pinned Docker container
- [ ] Git tag flow works (tag created with correct format `v{version}`)
- [ ] SNAPSHOT bump produces correct next version in pom.xml files
