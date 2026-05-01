# OpenCode Server Docker Setup

Docker configuration for running OpenCode server with Z.AI provider integration.

## Purpose

This directory contains the Docker Compose setup for running the OpenCode server locally. The server provides the API that the OpenCode Java SDK connects to.

## Architecture

```mermaid
flowchart TB
    subgraph "Docker Infrastructure"
        SERVER["OpenCode Server<br/>Port: 4096"]
        CONFIG["Configuration<br/>config/"]
        ENV["Environment<br/>.env.opencode"]
    end

    subgraph "Z.AI Provider"
        ZAI["Z.AI API<br/>GLM-4.7 Model"]
        API_KEY["API Key"]
    end

    subgraph "Client SDK"
        SDK["OpenCode Java SDK"]
    end

    SERVER --> CONFIG
    SERVER --> ENV
    SERVER --> ZAI
    ZAI --> API_KEY
    SDK --> SERVER

    style SERVER fill:#e3f2fd
    style ZAI fill:#fff3e0
    style SDK fill:#e8f5e9
```

## Components

### Docker Compose
- **File**: `docker-compose.yml`
- **Service**: `opencode-server`
- **Network**: `opencode-sdk` (bridge)

### Configuration Files

| File | Purpose |
|------|---------|
| `Dockerfile` | Container definition |
| `config/opencode.json.template` | Template for OpenCode server configuration |
| `config/auth.json.template` | Template for authentication configuration |
| `start.sh` | Startup script with environment variable handling and template processing |
| `.env.opencode` | Environment variables for Docker Compose |
| `.env.opencode.example` | Example environment file with all required variables |
|

## Default Configuration

### Server Settings
- **Port**: 4096 (HTTP API)
- **Health Endpoint**: `GET /global/health`
- **Documentation**: `http://localhost:4096/doc`

### Authentication
- **Type**: HTTP Basic Auth
- **Default Username**: `opencode`
- **Default Password**: `opencode123`

### Z.AI Provider
- **Model**: GLM-4.7
- **Base URL**: https://api.z.ai/api/coding/paas/v4
- **Max Tokens**: 128000
- **Temperature**: 0.7

## Quick Start

### Start the Server

```bash
# Start services
docker-compose up -d

# Build and start
docker-compose up --build -d

# View logs
docker-compose logs -f opencode-server
```

### Verify Installation

```bash
# Health check
curl http://localhost:4096/global/health

# With authentication
curl -u opencode:opencode123 http://localhost:4096/global/health

# View API docs
open http://localhost:4096/doc
```

## Environment Variables

| Variable | Description | Default |
|----------|-------------|---------|
| `OPENCODE_SERVER_USERNAME` | HTTP Basic Auth username | opencode |
| `OPENCODE_SERVER_PASSWORD` | HTTP Basic Auth password | opencode123 |
| `Z_AI_API_KEY` | Z.AI API key | (pre-configured) |

## Directory Structure

```
docker/opencode/
├── Dockerfile
├── docker-compose.yml
├── start.sh
├── .env
├── .env.opencode
├── .env.opencode.example
├── .dockerignore
├── config/
│   ├── opencode.json.template
│   └── auth.json.template
└── README.md
```

## Volumes

| Volume | Mount Point | Purpose |
|--------|-------------|---------|
| `opencode-data` | `/app/data` | Persistent data storage |
| `opencode-config` | `/root/.config/opencode` | Configuration files |

## Networking

- **Network Name**: `opencode-sdk`
- **Driver**: Bridge
- **Exposed Port**: 4096

## Security Considerations

1. **Change Default Credentials**
   - Update `.env.opencode` with strong password
   - Do not commit `.env.opencode` to version control

2. **API Keys**
   - Store API keys in environment variables
   - Never hardcode credentials in configuration files

3. **Network Access**
   - Server binds to all interfaces (0.0.0.0)
   - Use firewall rules to restrict access in production

## Troubleshooting

### Check Container Status
```bash
docker-compose ps
docker-compose logs opencode-server
```

### Test Z.AI Connectivity
```bash
docker exec opencode-server \
  curl -H "Authorization: Bearer <api-key>" \
  https://api.z.ai/api/paas/v4/models
```

### Restart Services
```bash
docker-compose restart
docker-compose down && docker-compose up -d
```

## Resources

- OpenAPI Spec: `openapi.json` (310KB)
- Health Check: `http://localhost:4096/global/health`
- API Documentation: `http://localhost:4096/doc`
