# OpenCode Server Docker Setup

This directory contains the Docker configuration for running OpenCode server with Z.AI provider integration.

## Quick Start

1. **Create environment configuration file:**
   ```bash
   cp .env.opencode.example .env.opencode
   ```

2. **Edit .env.opencode with your credentials:**
   - Set `Z_AI_API_KEY` to your Z.AI API key
   - Set `OPENCODE_SERVER_USERNAME` and `OPENCODE_SERVER_PASSWORD` for HTTP basic auth

3. **Build and start the services:**
   ```bash
   docker-compose up --build
   ```

   The Docker image is pinned to a specific OpenCode version via the `OPENCODE_VERSION` build arg. By default, it reads from `docker-compose.yml` which mirrors the version in [`.opencode-version`](../../.opencode-version) at the project root. To change the target version, edit `.opencode-version` and rebuild.

4. **Verify OpenCode server is running:**
   ```bash
   curl http://localhost:4096/global/health
   ```

5. **Check OpenAPI documentation:**
   Open http://localhost:4096/doc in your browser

## Configuration Methods

### Method 1: Environment File (Recommended)

Create a `.env.opencode` file in this directory:

```bash
cp .env.opencode.example .env.opencode
# Edit with your credentials
docker-compose up --build
```

### Method 2: Command Line Environment Variables

Pass variables directly to docker-compose:

```bash
Z_AI_API_KEY=your_key_here \
OPENCODE_SERVER_USERNAME=admin \
OPENCODE_SERVER_PASSWORD=secret \
OPENCODE_VERSION=1.17.1 \
docker-compose up --build
```

Override `OPENCODE_VERSION` to build with a different OpenCode version. The default is read from the project root [`.opencode-version`](../../.opencode-version) file.

### Method 3: Docker Run

Use `docker run` with environment flags:

```bash
docker run -p 4096:4096 \
  -e Z_AI_API_KEY=your_key_here \
  -e OPENCODE_SERVER_USERNAME=admin \
  -e OPENCODE_SERVER_PASSWORD=secret \
  opencode-server:${OPENCODE_VERSION}
```

## Environment Variables

### Required Variables

These variables **MUST** be provided at runtime. The container will fail to start without them:

| Variable | Description | Example |
|----------|-------------|---------|
| `Z_AI_API_KEY` | Your Z.AI provider API key | `a039df3a972e4c91a2a190...` |
| `OPENCODE_SERVER_USERNAME` | Username for HTTP basic authentication | `opencode` |
| `OPENCODE_SERVER_PASSWORD` | Password for HTTP basic authentication | `opencode123` |

### Build Arguments

| Variable | Description | Source |
|----------|-------------|--------|
| `OPENCODE_VERSION` | OpenCode server version to install | [`.opencode-version`](../../.opencode-version) |

The Dockerfile installs the exact version specified by `OPENCODE_VERSION` and verifies the installed version matches. If there is a mismatch, the build fails.

### Optional Variables

| Variable | Description | Default |
|----------|-------------|--------|
| `COMPOSE_PROJECT_NAME` | Docker Compose project name | `opencode-sdk` |

### Files

- `Dockerfile`: Container definition with version-pinned OpenCode install and verification
- `config/opencode.json.template`: Template for OpenCode configuration with Z.AI provider
- `config/auth.json.template`: Template for authentication configuration
- `start.sh`: Startup script with environment variable handling
- `.env.opencode.example`: Example environment file with all required variables

### Version Management

The OpenCode version is pinned via the `OPENCODE_VERSION` build arg:

1. The project root [`.opencode-version`](../../.opencode-version) file is the single source of truth
2. `docker-compose.yml` passes `OPENCODE_VERSION` as a build arg (defaults to the value in the compose file)
3. The `Dockerfile` installs that exact version and verifies it matches

To change the target OpenCode version:

```bash
# Edit .opencode-version at the project root
echo "1.18.0" > ../../.opencode-version

# Update the default in docker-compose.yml to match
# Then rebuild
docker-compose up --build
```

## Z.AI Provider Configuration

The container is pre-configured with:
- **Model**: GLM-4.7
- **Base URL**: https://api.z.ai/api/coding/paas/v4
- **Max Tokens**: 128000
- **Temperature**: 0.7

The Z.AI API key is provided via the `Z_AI_API_KEY` environment variable at runtime.

## Health Checks

The container includes health checks that verify:
- OpenCode server is responding on port 4096
- Health endpoint returns successful response
- Service is ready to accept requests

## Volumes

- `opencode-data`: Persistent storage for OpenCode data
- `opencode-config`: Configuration files and settings

## Networking

The service runs on the `opencode-sdk` bridge network and exposes:
- Port 4096: OpenCode server HTTP API

## Security Best Practices

- **Never commit credentials**: The `.env.opencode` file is already in `.gitignore`
- **Use strong passwords**: Choose secure values for `OPENCODE_SERVER_USERNAME` and `OPENCODE_SERVER_PASSWORD`
- **Protect your Z.AI API key**: Treat it like a password - don't share or expose it
- **Rotate credentials regularly**: Update your API keys and passwords periodically
- **Use environment-specific credentials**: Different keys for development vs production

## Troubleshooting

1. **Check container logs:**
   ```bash
   docker-compose logs opencode-server
   ```

2. **Verify Z.AI connectivity:**
   ```bash
   # Replace YOUR_Z_AI_API_KEY with your actual key
   docker exec opencode-server curl -H "Authorization: Bearer YOUR_Z_AI_API_KEY" https://api.z.ai/api/paas/v4/models
   ```

3. **Test authentication:**
   ```bash
   # Replace username:password with your configured credentials
   curl -u username:password http://localhost:4096/global/health
   ```

4. **Missing environment variables:**
   If you see errors about missing `Z_AI_API_KEY`, `OPENCODE_SERVER_USERNAME`, or `OPENCODE_SERVER_PASSWORD`, ensure:
   - The `.env.opencode` file exists and is properly configured
   - Or the environment variables are set before running docker-compose
   - The variables are exported (use `export VAR=value` in shell)
