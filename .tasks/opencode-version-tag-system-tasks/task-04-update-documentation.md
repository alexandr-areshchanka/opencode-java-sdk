# Task 04: Update Project Documentation

**Type:** Code Modification

## Goal

Document the new `.opencode-version` system, version pinning, and updated release process across all relevant project documentation.

## What to Do

- Update root `README.md`:
  - Add `.opencode-version` to project structure overview
  - Update Build Commands section to mention version pinning
  - Rewrite Release Scripts section to document new flow: edit `.opencode-version` → run release script → tests gate → tag
  - Note that Docker builds are reproducible (pinned version)
- Update root `AGENTS.md`:
  - Add `.opencode-version` to project context (under Key Components or similar section)
  - Update release flow description to reflect version-aware pipeline
  - Document that integration tests are mandatory gate
- Update `docker/opencode/README.md`:
  - Document `OPENCODE_VERSION` build arg
  - Explain `.opencode-version` as source of truth
  - How to change target OpenCode version (edit `.opencode-version`)
  - Version verification step in Dockerfile
- Update `docker/opencode/AGENTS.md`:
  - Document Docker version pinning and build arg
  - Update any references to "latest" installs

## Files/Areas

- `README.md` — Release Scripts section, Build Commands section, project structure
- `AGENTS.md` — Project overview, key components, release flow
- `docker/opencode/README.md` — Docker setup instructions, version pinning
- `docker/opencode/AGENTS.md` — Docker infrastructure documentation

## Key Points

- Emphasize `.opencode-version` as the single source of truth for OpenCode version across the entire project
- Document the complete release flow: edit `.opencode-version` → run release script → Docker build with pinned version → verify version → build SDK → run integration tests → tag → bump SNAPSHOT
- Note that integration tests are a mandatory gate — releases cannot proceed without passing tests
- Remove or update any documentation that says Docker "always installs latest"
- The `.opencode-version` file pattern is familiar from `.nvmrc`, `.ruby-version`, `.tool-versions` — mention this analogy for clarity
- Do NOT add JavaDocs to classes and methods (per project convention)

## Done When

- [ ] README.md documents `.opencode-version` and updated release flow
- [ ] AGENTS.md updated with new release pipeline description
- [ ] Docker README documents version pinning and `OPENCODE_VERSION` build arg
- [ ] No references to old "always latest" Docker behavior remain in any documentation file
