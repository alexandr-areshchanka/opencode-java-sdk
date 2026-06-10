# opencode-java-sdk

Java SDK for [OpenCode](https://github.com/anomalyco/opencode) server API with examples and Spring Boot Starter.

## OpenCode Server

The SDK connects to an [OpenCode](https://github.com/anomalyco/opencode) server instance. The server source is included as a git submodule at the repository root (`opencode/`).

To clone this repository with the submodule:

```bash
git clone --recursive https://github.com/anomalyco/opencode-java-sdk.git
```

<arg_value>Or to initialize the submodule in an existing clone:

```bash
git submodule update --init --recursive
```

For more details on Docker setup, see [`docker/opencode/README.md`](docker/opencode/README.md).
For the OpenCode server source, see [`opencode/`](opencode/).

## Version Management

The file [`.opencode-version`](.opencode-version) is the single source of truth for the OpenCode server version (similar to `.nvmrc`, `.ruby-version`, `.tool-versions`). Docker builds read this file to pin the exact OpenCode version, ensuring reproducible builds.

## Build

```bash
mvn clean install -DskipTests -pl sdk
```

Docker builds use the version from `.opencode-version` — no manual version arguments needed.

## Release

1. Edit `.opencode-version` to set the target version (or pass it as an argument)
2. Run `./release.sh` (Linux/macOS) or `release.bat` (Windows)
3. The script will:
   - Build Docker image with the pinned version
   - Verify the installed version matches
   - Build the SDK
   - Run integration tests (mandatory gate — release aborts on failure)
   - Create a git tag
   - Bump `.opencode-version` to the next `-SNAPSHOT`
4. After a successful release, push manually: `git push && git push --tags`

```bash
# Release current version from .opencode-version
./release.sh

# Override version for this release
./release.sh 1.18.0
```
