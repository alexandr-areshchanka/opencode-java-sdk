# Task 04: Verify Compilation

**Type:** Verification
**Suggested agent:** Code

## Goal
Run Maven compilation across all three affected modules to confirm zero compile errors after the migration fixes.

## Why This Task Exists
After Tasks 01–03 fix the code, we need a final verification that everything compiles cleanly together.

## Spec Coverage
- Requirements: R1, R2, R3 (verification)
- Scenarios: S1, S2, S3

## Required Inputs
- All code changes from Tasks 01–03 must be completed

## Files/Areas
- `opencode-spring-boot-starter/` — verify starter compiles
- `examples/plain-java/` — verify plain-java examples compile
- `examples/spring-boot/` — verify spring-boot examples compile

## Constraints / Non-Goals
- This is a read-only verification task — no code changes expected
- If compile errors are found, list them precisely for the user to decide on next steps
- Do NOT run tests (project policy: no tests unless explicitly asked)

## Output Artifacts
- `.tasks/migration-review-examples-tasks/compilation-result.md` — compilation output summary

## What to Do
1. Run `mvn compile -pl opencode-spring-boot-starter` and capture result
2. Run `mvn compile -pl examples/plain-java -am` and capture result
3. Run `mvn compile -pl examples/spring-boot -am` and capture result
4. Write a summary to `compilation-result.md` with pass/fail for each module

## Expected Output
- All three modules compile successfully with zero errors
- If errors remain, provide the exact error messages and affected files

## Acceptance Criteria
- [ ] `opencode-spring-boot-starter` compiles successfully
- [ ] `examples/plain-java` compiles successfully (with dependencies)
- [ ] `examples/spring-boot` compiles successfully (with dependencies)
- [ ] Results documented in `compilation-result.md`
- [ ] Covered requirements and scenarios are satisfied
