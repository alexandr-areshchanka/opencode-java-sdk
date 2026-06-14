# OpenCode Plain Java Example

This module demonstrates how to use the OpenCode Java SDK in a plain Java application without Spring Boot.

## Prerequisites

Before running the examples, ensure you have:

1. **Java 25** or higher installed
2. **Maven 3.x** installed
3. **OpenCode Server** running on port 4096

### Starting the OpenCode Server

See the [docker/opencode/README.md](../../docker/opencode/README.md) for instructions on starting the OpenCode server via Docker.

Quick start:
```bash
cd docker/opencode
docker-compose up -d
```

The server should be accessible at `http://localhost:4096` with default credentials:
- Username: `opencode`
- Password: `opencode123`

## Project Structure

```
src/main/java/opencode/examples/plainjava/
├── Main.java              # Entry point with health check
├── SessionCrudExample.java # Session CRUD operations demo
└── MessageExample.java     # Message handling demo
```

## How to Compile

### From the Module Directory

```bash
cd examples/plain-java
mvn clean compile
```

### Compile with All Dependencies

To ensure all dependencies (including the SDK module) are built:

```bash
cd examples/plain-java
mvn clean compile -am
```

### Full Build with Packaging

To create the executable JAR:

```bash
cd examples/plain-java
mvn clean package -DskipTests
```

The JAR will be created at:
```
target/opencode-examples-plain-java-0.1.0-SNAPSHOT.jar
```

## How to Run

### Option 1: Run with Maven

```bash
cd examples/plain-java
mvn exec:java -Dexec.mainClass="opencode.examples.plainjava.Main"
```

Or simply:

```bash
cd examples/plain-java
mvn compile exec:java
```

### Option 2: Run the JAR

First, ensure the SDK JAR is in your classpath:

```bash
cd examples/plain-java
java -cp "target/opencode-examples-plain-java-0.1.0-SNAPSHOT.jar;target/dependency/*" opencode.examples.plainjava.Main
```

Or copy dependencies first:

```bash
cd examples/plain-java
mvn dependency:copy-dependencies
java -cp "target/opencode-examples-plain-java-0.1.0-SNAPSHOT.jar;target/dependency/*" opencode.examples.plainjava.Main
```

### Option 3: From IDE

1. Import the project as a Maven project
2. Navigate to `Main.java`
3. Run the `main` method

## Expected Output

When the examples run successfully, you should see output similar to:

```
[main] INFO opencode.examples.plainjava.Main - Starting OpenCode Java SDK Examples
[main] INFO opencode.examples.plainjava.Main - ====================================
[main] INFO opencode.examples.plainjava.Main - 
[main] INFO opencode.examples.plainjava.Main - --- Health Check ---
[main] INFO opencode.examples.plainjava.Main - Health check successful!
[main] INFO opencode.examples.plainjava.Main -   Healthy: true
[main] INFO opencode.examples.plainjava.Main -   Version: 1.0.0
[main] INFO opencode.examples.plainjava.Main - 
[main] INFO opencode.examples.plainjava.Main - ========================================
[main] INFO opencode.examples.plainjava.SessionCrudExample - --- Session CRUD Example ---
[main] INFO opencode.examples.plainjava.SessionCrudExample - Creating a new session...
[main] INFO opencode.examples.plainjava.SessionCrudExample - Session created with ID: <session-id>
...
[main] INFO opencode.examples.plainjava.Main - ====================================
[main] INFO opencode.examples.plainjava.Main - All examples completed successfully!
```

## Examples Overview

### SessionCrudExample

Demonstrates:
- Creating a new session
- Retrieving session by ID
- Listing all sessions
- Updating session
- Deleting session

### MessageExample

Demonstrates:
- Sending user messages
- Receiving assistant responses
- Handling message parts

## Troubleshooting

### Connection Refused Error

```
Cannot connect to OpenCode server. Please ensure the server is running at http://localhost:4096
```

**Solution:**
1. Check if Docker is running: `docker ps`
2. Start the OpenCode server:
   ```bash
   cd docker/opencode
   docker-compose up -d
   ```
3. Verify the server is healthy:
   ```bash
   curl http://localhost:4096/global/health
   ```

### Authentication Failed

If you see 401 errors, verify the credentials in `Main.java`:

```java
config.setUsername("opencode");
config.setPassword("opencode123");
```

Check the actual credentials in `docker/opencode/config/auth.json`.

### Compilation Errors

**SDK not found:**
Ensure the SDK module is built:
```bash
cd sdk
mvn clean install
```

Then retry building the examples.

**Java version mismatch:**
Ensure you're using Java 25:
```bash
java -version
```

### Port Already in Use

If port 4096 is already in use:
1. Stop the existing container: `docker stop <container-id>`
2. Or change the port in `docker-compose.yml`:
   ```yaml
   ports:
     - "4097:4096"  # Use 4097 instead
   ```
   Then update the URL in `Main.java`:
   ```java
   config.setBaseUrl("http://localhost:4097");
   ```

## Configuration

The example uses programmatic configuration:

```java
OpenCodeConfig config = new OpenCodeConfig();
config.setBaseUrl("http://localhost:4096");
config.setUsername("opencode");
config.setPassword("opencode123");
```

You can modify these values to match your server setup.

## Dependencies

- `opencode-sdk` - The core OpenCode SDK
- `slf4j-simple` - Simple logging facade (transitive from SDK)
- Jackson libraries - JSON processing (transitive from SDK)

## Additional Resources

- [OpenCode Java SDK Documentation](../../sdk/README.md)
- [Spring Boot Example](../spring-boot/README.md)
- [OpenCode Server Docker Setup](../../docker/opencode/README.md)
- [OpenAPI Specification](../../docker/opencode/openapi.json)
