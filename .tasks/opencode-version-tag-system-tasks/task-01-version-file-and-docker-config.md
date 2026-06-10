# Task 01: Create `.opencode-version` and Update Docker Config

**Type:** Code Modification

## Goal

Establish `.opencode-version` as the single source of truth for OpenCode version, and wire it into Docker builds and test infrastructure.

## What to Do

- Create `.opencode-version` at project root containing the current target version (e.g., `1.17.1`)
- Update `docker/opencode/Dockerfile`: add `ARG OPENCODE_VERSION` and pass it to the install script as `bash -s -- --version ${OPENCODE_VERSION}`
- Update `docker/opencode/docker-compose.yml`: add `OPENCODE_VERSION` build arg, read it from env or default
- Update `examples/spring-boot/src/test/java/.../OpenCodeServerContainer.java`: tag the Docker image with the version (e.g., `opencode-server:${version}` instead of `opencode-server:test`) so testcontainers runs against the correct pinned image
- Update `examples/spring-boot/pom.xml` `build-docker-image` profile to pass `OPENCODE_VERSION` build arg
- Add a verification step in Dockerfile that confirms the installed OpenCode version matches the requested version (fail the build if mismatch)

## Files/Areas

- `.opencode-version` — New file, single line version string (no `v` prefix, no newline trivia)
- `docker/opencode/Dockerfile` — Add ARG, pass to install script, add version verification
- `docker/opencode/docker-compose.yml` — Pass build arg
- `examples/spring-boot/src/test/java/opencode/examples/springboot/testsupport/OpenCodeServerContainer.java` — Use versioned image name (read from `.opencode-version` or system property)
- `examples/spring-boot/pom.xml` — Pass `OPENCODE_VERSION` to Docker build plugin in `build-docker-image` profile

## Key Points

- The `.opencode-version` file contains a plain semver string with no prefix (e.g., `1.17.1`, not `v1.17.1`)
- Dockerfile must fail the build if the installed version doesn't match the requested version (add a verification step after install)
- The install script supports `--version` flag: `curl -fsSL https://opencode.ai/install | bash -s -- --version 1.17.1`
- The install script also supports `VERSION` env var as an alternative
- Current Dockerfile uses `FROM node:24-alpine` and installs via `curl -fsSL https://opencode.ai/install | bash`
- After install, binary is at `/root/.opencode/bin/opencode` — use `opencode --version` or check package.json for verification
- The existing `OpenCodeServerContainer` uses image name `opencode-server:test` — this should become versioned (e.g., `opencode-server:1.17.1`)
- The `build-docker-image` Maven profile in `examples/spring-boot/pom.xml` builds from `docker/opencode/Dockerfile` — needs to pass `OPENCODE_VERSION` as build arg

## Done When

- [ ] `.opencode-version` file exists at project root with a valid semver string
- [ ] `docker compose build` reads the version and pins the install
- [ ] Running container reports the exact version from `.opencode-version`
- [ ] Dockerfile has a verification step that fails if installed version mismatches
- [ ] `OpenCodeServerContainer` references a versioned image tag
- [ ] All changes compile without errors
