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

## Build

```bash
mvn clean install -DskipTests -pl sdk
```
