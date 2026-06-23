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

## Running the OpenCode Server (Docker)

The SDK and examples require a running OpenCode server. The Docker configuration lives in [`docker/opencode/`](docker/opencode/).

**1. Configure credentials** — create an environment file from the template and fill in your Z.AI API key and server credentials:

```bash
cd docker/opencode
cp .env.opencode.example .env.opencode
```

Required values in `.env.opencode`:

| Variable | Description |
|----------|-------------|
| `Z_AI_API_KEY` | Your Z.AI provider API key |
| `OPENCODE_SERVER_USERNAME` | HTTP Basic Auth username (default: `opencode`) |
| `OPENCODE_SERVER_PASSWORD` | HTTP Basic Auth password (default: `opencode123`) |

**2. Build and start the server** — the image is built at the version pinned in [`.opencode-version`](.opencode-version):

```bash
cd docker/opencode
docker-compose up --build -d
```

**3. Verify it is healthy:**

```bash
curl -u opencode:opencode123 http://localhost:4096/global/health
```

The server is now available at `http://localhost:4096`, and interactive API docs are served at http://localhost:4096/doc. Stop it with `docker-compose down` (run from the `docker/opencode/` directory). See [`docker/opencode/README.md`](docker/opencode/README.md) for the full setup guide.

## Build

```bash
mvn clean install -DskipTests -pl sdk
```

Docker builds use the version from `.opencode-version` — no manual version arguments needed.

## Running the Examples

Both examples require a running OpenCode server (see above) and the SDK installed locally first:

```bash
mvn clean install -DskipTests -pl sdk
```

### Plain Java Example

Runs 18 SDK examples against a live server using the SDK directly (no Spring). It connects to `http://localhost:4096` with credentials `opencode`/`opencode123`.

```bash
cd examples/plain-java
mvn exec:java -Dexec.mainClass="opencode.examples.plainjava.Main"
```

Alternatively, build the executable JAR with `mvn clean package -DskipTests`. The runnable artifact is the main `opencode-examples-plain-java-*.jar` in `target/` (not the `-test-runner` JARs). See [`examples/plain-java/README.md`](examples/plain-java/README.md).

### Spring Boot Example

A Spring Boot REST application (17 controllers) that exposes the full SDK over HTTP, running on port 8080.

```bash
cd examples/spring-boot
mvn spring-boot:run
```

Alternatively, build the JAR and run it:

```bash
cd examples/spring-boot
mvn clean package -DskipTests
java -jar target/opencode-examples-spring-boot-*.jar
```

Once started, the REST API is available at http://localhost:8080:

- Health: http://localhost:8080/api/system/health
- Sessions: http://localhost:8080/api/sessions

The server connection can be overridden via environment variables `OPENCODE_BASE_URL`, `OPENCODE_USERNAME`, and `OPENCODE_PASSWORD`, or in `application.properties`. See [`examples/spring-boot/AGENTS.md`](examples/spring-boot/AGENTS.md).

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
