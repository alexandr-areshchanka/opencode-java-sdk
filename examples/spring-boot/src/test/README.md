# Integration Tests

This directory contains integration tests for the OpenCode Spring Boot example application.

## Overview

Integration tests use Testcontainers to spin up an OpenCode server in a Docker container for testing. The tests verify that the REST API endpoints work correctly with a real OpenCode server instance.

## Prerequisites

- Docker installed and running
- Maven 3.x
- Java 25
- Z_AI_API_KEY environment variable set (or default will be used)

## Quick Start

### 1. Build the Docker Image

You need to build the OpenCode server Docker image before running tests. There are three ways:

#### Option A: Using the Shell Script (Linux/macOS/WSL)
```bash
cd examples/spring-boot
./build-docker-image.sh
```

#### Option B: Using the Batch Script (Windows)
```cmd
cd examples\spring-boot
build-docker-image.bat
```

#### Option C: Using Maven
```bash
cd examples/spring-boot
mvn clean verify -Pbuild-docker-image
```

### 2. Run Integration Tests

```bash
cd examples/spring-boot
mvn clean test -DrunIntegrationTests
```

Or to run a specific test:
```bash
mvn clean test -Dtest=ConfigurationControllerIT -DrunIntegrationTests
```

## Container Reuse for Faster Development

By default, containers are not reused between test runs. To enable container reuse (faster subsequent test runs):

1. Edit `src/test/resources/.testcontainers.properties`:
```properties
testcontainers.reuse.enable=true
```

2. Run tests - the container will stay running between test executions

3. To stop reused containers when done:
```bash
./stop-reused-container.sh
```

**Note:** Container reuse is automatically disabled in CI environments (GitHub Actions, GitLab CI, Jenkins).

## Troubleshooting

### "Docker image 'opencode-server:test' not found"

Build the Docker image first using one of the methods in the Quick Start section.

### "Docker daemon is not running"

Start Docker Desktop or your Docker daemon before running tests.

### Container startup timeout

If the container fails to start within 30 seconds:
1. Check Docker resources (CPU/memory)
2. Verify the image was built correctly
3. Check Docker logs: `docker logs <container-id>`

### Port conflicts

The container uses port 4096 internally. Testcontainers will map this to a random port on the host.

## Configuration

### Environment Variables

- `Z_AI_API_KEY` - API key for Z.AI provider (optional, default is used if not set)

### System Properties

- `testcontainers.reuse.enable` - Enable container reuse (default: false)

## Available Test Classes

- `ConfigurationControllerIT` - Tests configuration endpoints
- `DevToolsControllerIT` - Tests dev tools endpoints
- `FileOperationsControllerIT` - Tests file operations
- `InstanceControllerIT` - Tests instance management
- `MessageControllerIT` - Tests message operations
- `ProjectControllerIT` - Tests project management
- `ProviderControllerIT` - Tests provider configuration
- `SessionAdvancedControllerIT` - Tests advanced session operations
- `SessionCrudControllerIT` - Tests basic session CRUD
- `SystemInfoControllerIT` - Tests system information
- `TodoControllerIT` - Tests todo operations
- `VcsControllerIT` - Tests VCS operations

## Build Scripts

### build-docker-image.sh / build-docker-image.bat

Builds the OpenCode server Docker image with optional parameters:
```bash
./build-docker-image.sh [IMAGE_NAME] [IMAGE_TAG]
# Default: opencode-server:test
```

### stop-reused-container.sh / stop-reused-container.bat

Stops and removes reused containers:
```bash
./stop-reused-container.sh [IMAGE_NAME]
# Default: opencode-server
```

## Maven Profiles

### build-docker-image

Builds the Docker image during the Maven build:
```bash
mvn clean verify -Pbuild-docker-image
```

### integration-tests

Runs integration tests (activated automatically with `-DrunIntegrationTests`):
```bash
mvn clean test -DrunIntegrationTests
```
