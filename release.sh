#!/bin/bash

# OpenCode Java SDK Release Script
# Automates the release process: rebuild Docker, download OpenAPI, extract version, update POMs, build SDK

set -euo pipefail

# Script directory and project root
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}"

# Configuration
CONTAINER_NAME="opencode-server"
DOCKER_DIR="${PROJECT_ROOT}/docker/opencode"
SDK_DIR="${PROJECT_ROOT}/sdk"
OPENCODE_PORT="${OPENCODE_SERVER_PORT:-4096}"
OPENCODE_USERNAME="${OPENCODE_SERVER_USERNAME:-opencode}"
OPENCODE_PASSWORD="${OPENCODE_SERVER_PASSWORD:-opencode123}"
HEALTH_URL="http://localhost:${OPENCODE_PORT}/global/health"
OPENAPI_URL="http://localhost:${OPENCODE_PORT}/doc"

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Logging functions
log_info() {
    echo -e "${BLUE}[INFO]${NC} $1"
}

log_success() {
    echo -e "${GREEN}[SUCCESS]${NC} $1"
}

log_warning() {
    echo -e "${YELLOW}[WARNING]${NC} $1"
}

log_error() {
    echo -e "${RED}[ERROR]${NC} $1"
}

# =============================================================================
# STEP 1: Rebuild Docker Image and Start Container
# =============================================================================

rebuild_docker() {
    log_info "=== STEP 1: Rebuild Docker Image and Start Container ==="

    # Check for .env.opencode file
    if [[ ! -f "${DOCKER_DIR}/.env.opencode" ]]; then
        log_error "Required file not found: ${DOCKER_DIR}/.env.opencode"
        log_info "Please create .env.opencode based on .env.opencode.example"
        exit 1
    fi

    log_info "Found .env.opencode configuration file"

    # Copy .env.opencode to .env so docker compose can auto-load it
    cp -f "${DOCKER_DIR}/.env.opencode" "${DOCKER_DIR}/.env"
    log_info "Copied .env.opencode to .env"

    # Change to docker directory
    cd "${DOCKER_DIR}"

    # Stop existing container if running
    log_info "Stopping existing container..."
    docker compose down 2>/dev/null || true

    # Remove any existing containers with the same name (not just stop them)
    log_info "Removing any existing containers..."
    docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true

    # Rebuild image from scratch
    log_info "Building Docker image (no cache)..."
    docker compose build --no-cache

    # Start container
    log_info "Starting Docker container..."
    docker compose up -d

    # Return to project root
    cd "${PROJECT_ROOT}"

    log_success "Docker container started successfully"
}

# =============================================================================
# STEP 2: Health Check & Download OpenAPI JSON
# =============================================================================

wait_for_healthy() {
    log_info "=== STEP 2: Wait for Server Health Check ==="

    local max_attempts=60
    local attempt=0

    log_info "Waiting for server at ${HEALTH_URL}..."

    while [[ $attempt -lt $max_attempts ]]; do
        if curl -sf -u "${OPENCODE_USERNAME}:${OPENCODE_PASSWORD}" "${HEALTH_URL}" > /dev/null 2>&1; then
            log_success "Server is healthy"
            return 0
        fi

        attempt=$((attempt + 1))
        log_info "Attempt $attempt/$max_attempts: Server not ready yet, waiting 5 seconds..."
        sleep 5
    done

    log_error "Server failed to become healthy after $max_attempts attempts"
    log_info "=== Docker Container Logs ==="
    docker logs "${CONTAINER_NAME}" 2>&1 || true
    log_info "=== End of Docker Logs ==="
    exit 1
}

download_openapi() {
    log_info "=== STEP 2b: Download OpenAPI JSON Spec ==="

    log_info "Downloading OpenAPI spec from ${OPENAPI_URL}..."

    if ! curl -sf -u "${OPENCODE_USERNAME}:${OPENCODE_PASSWORD}" "${OPENAPI_URL}" -o "${SDK_DIR}/openapi.json"; then
        log_error "Failed to download OpenAPI spec"
        exit 1
    fi

    # Validate the downloaded file
    if [[ ! -s "${SDK_DIR}/openapi.json" ]]; then
        log_error "Downloaded OpenAPI file is empty"
        exit 1
    fi

    if ! head -c 1 "${SDK_DIR}/openapi.json" | grep -q '{'; then
        log_error "Downloaded file does not appear to be valid JSON"
        exit 1
    fi

    local file_size
    file_size=$(du -h "${SDK_DIR}/openapi.json" | cut -f1)
    log_success "OpenAPI spec downloaded successfully (${file_size})"
}

# =============================================================================
# STEP 3: Extract OpenCode Version from Container
# =============================================================================

extract_version() {
    log_info "=== STEP 3: Extract OpenCode Version from Container ==="

    local extracted_version

    # Extract version from container's package.json
    log_info "Attempting to extract version from container's package.json..."

    extracted_version=$(docker exec "${CONTAINER_NAME}" cat /root/.config/opencode/package.json 2>/dev/null | grep -o '"@opencode-ai/plugin": "[^"]*"' | grep -o '[0-9]\+\.[0-9]\+\.[0-9]\+')

    if [[ -z "${extracted_version}" ]]; then
        log_error "Failed to extract version from /root/.config/opencode/package.json"
        exit 1
    fi

    # Validate semver format
    if ! [[ "${extracted_version}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        log_error "Extracted version '${extracted_version}' does not match semver pattern"
        exit 1
    fi

    OPENCODE_VERSION="${extracted_version}"
    log_success "Extracted OpenCode version: ${OPENCODE_VERSION}"
}

# =============================================================================
# STEP 4: Update Version in pom.xml Files
# =============================================================================

update_version() {
    log_info "=== STEP 4: Update Version in pom.xml Files ==="

    log_info "Updating root pom.xml with version ${OPENCODE_VERSION}..."

    # Detect OS for sed compatibility
    local sed_inplace
    if [[ "$(uname)" == "Darwin" ]]; then
        sed_inplace="sed -i ''"
    else
        sed_inplace="sed -i"
    fi

    # Update root pom.xml revision property
    ${sed_inplace} "s/<revision>.*<\/revision>/<revision>${OPENCODE_VERSION}<\/revision>/" "${PROJECT_ROOT}/pom.xml"

    # Verify the update
    if grep -q "<revision>${OPENCODE_VERSION}</revision>" "${PROJECT_ROOT}/pom.xml"; then
        log_success "Root pom.xml updated successfully"
    else
        log_error "Failed to update root pom.xml"
        exit 1
    fi

    # Update examples/spring-boot/pom.xml (it defines its own revision)
    log_info "Updating examples/spring-boot/pom.xml..."
    ${sed_inplace} "s/<revision>.*<\/revision>/<revision>${OPENCODE_VERSION}<\/revision>/" "${PROJECT_ROOT}/examples/spring-boot/pom.xml"

    if grep -q "<revision>${OPENCODE_VERSION}</revision>" "${PROJECT_ROOT}/examples/spring-boot/pom.xml"; then
        log_success "examples/spring-boot/pom.xml updated successfully"
    else
        log_error "Failed to update examples/spring-boot/pom.xml"
        exit 1
    fi

    log_success "All pom.xml files updated to version ${OPENCODE_VERSION}"
}

# =============================================================================
# STEP 5: Build and Verify SDK
# =============================================================================

build_sdk() {
    log_info "=== STEP 5: Build and Verify SDK ==="

    cd "${PROJECT_ROOT}"

    log_info "Running Maven build..."
    if ! mvn clean install -pl sdk -am -q; then
        log_error "Maven build failed"
        exit 1
    fi

    log_success "SDK built successfully"
}

# =============================================================================
# Main Execution
# =============================================================================

main() {
    echo "========================================"
    echo "OpenCode Java SDK Release Script"
    echo "========================================"
    echo ""

    rebuild_docker
    wait_for_healthy
    download_openapi
    extract_version
    update_version
    build_sdk

    echo ""
    echo "========================================"
    log_success "Release process completed successfully!"
    echo "========================================"
    echo ""
    echo "Summary:"
    echo "  - OpenCode version: ${OPENCODE_VERSION}"
    echo "  - OpenAPI spec: sdk/openapi.json"
    echo "  - pom.xml updated: ${OPENCODE_VERSION}"
    echo "  - SDK build: Verified"
    echo ""
}

# Run main function
main "$@"
