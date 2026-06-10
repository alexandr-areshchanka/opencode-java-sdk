#!/bin/bash

# OpenCode Java SDK — Version-Aware Release Pipeline
#
# Usage: ./release.sh [version]
#   If [version] is provided, .opencode-version is updated first.
#   Otherwise the version is read from .opencode-version.
#
# Flow: rebuild Docker → verify version → strip SNAPSHOT → build → test →
#       tag → bump → commit SNAPSHOT → summary
#
# Idempotent: safe to re-run if a step fails mid-way.

set -euo pipefail

# ---------------------------------------------------------------------------
# Paths
# ---------------------------------------------------------------------------
SCRIPT_DIR="$(cd "$(dirname "$0")" && pwd)"
PROJECT_ROOT="${SCRIPT_DIR}"
VERSION_FILE="${PROJECT_ROOT}/.opencode-version"
DOCKER_DIR="${PROJECT_ROOT}/docker/opencode"

# ---------------------------------------------------------------------------
# Configuration
# ---------------------------------------------------------------------------
CONTAINER_NAME="opencode-server"
OPENCODE_PORT="${OPENCODE_SERVER_PORT:-4096}"
OPENCODE_USERNAME="${OPENCODE_SERVER_USERNAME:-opencode}"
OPENCODE_PASSWORD="${OPENCODE_SERVER_PASSWORD:-opencode123}"
HEALTH_URL="http://localhost:${OPENCODE_PORT}/global/health"

POM_FILES=(
    "${PROJECT_ROOT}/pom.xml"
    "${PROJECT_ROOT}/examples/spring-boot/pom.xml"
)

# ---------------------------------------------------------------------------
# Colors
# ---------------------------------------------------------------------------
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m'

# ---------------------------------------------------------------------------
# Logging helpers
# ---------------------------------------------------------------------------
log_info()    { echo -e "${BLUE}[INFO]${NC} $1"; }
log_success() { echo -e "${GREEN}[SUCCESS]${NC} $1"; }
log_warning() { echo -e "${YELLOW}[WARNING]${NC} $1"; }
log_error()   { echo -e "${RED}[ERROR]${NC} $1"; }

# ---------------------------------------------------------------------------
# Platform helpers
# ---------------------------------------------------------------------------
detect_sed_inplace() {
    if [[ "$(uname)" == "Darwin" ]]; then
        SED_INPLACE="sed -i ''"
    else
        SED_INPLACE="sed -i"
    fi
}

sed_inplace() {
    # Usage: sed_inplace <pattern> <file>
    if [[ "$(uname)" == "Darwin" ]]; then
        sed -i '' "$1" "$2"
    else
        sed -i "$1" "$2"
    fi
}

# ---------------------------------------------------------------------------
# Version math — patch bump only
# ---------------------------------------------------------------------------
bump_patch() {
    local version="$1"
    local major minor patch
    IFS='.' read -r major minor patch <<< "${version}"
    patch=$((patch + 1))
    echo "${major}.${minor}.${patch}"
}

# ---------------------------------------------------------------------------
# POM helpers
# ---------------------------------------------------------------------------
current_pom_revision() {
    grep -oE '<revision>[^<]+</revision>' "$1" | head -1 | sed 's/<revision>//;s/<\/revision>//'
}

set_pom_revision() {
    local file="$1"
    local new_revision="$2"
    sed_inplace "s|<revision>[^<]*</revision>|<revision>${new_revision}</revision>|" "${file}"
}

# ---------------------------------------------------------------------------
# Git helpers
# ---------------------------------------------------------------------------
git_has_clean_version_commit() {
    # Returns 0 if the latest commit already sets the clean (non-SNAPSHOT) revision
    local version="$1"
    git log -1 --oneline --grep="Release v${version}" HEAD >/dev/null 2>&1
}

git_has_tag() {
    local version="$1"
    git rev-parse "v${version}" >/dev/null 2>&1
}

git_has_snapshot_commit() {
    local version="$1"
    git log -1 --oneline --grep="Bump to ${version}-SNAPSHOT" HEAD >/dev/null 2>&1
}

# ---------------------------------------------------------------------------
# STEP 1: Read / set target version
# ---------------------------------------------------------------------------
step_read_version() {
    log_info "=== STEP 1: Read target version ==="

    if [[ ! -f "${VERSION_FILE}" ]]; then
        log_error ".opencode-version file not found at ${VERSION_FILE}"
        exit 1
    fi

    TARGET_VERSION="$(head -1 "${VERSION_FILE}" | tr -d '[:space:]')"

    if [[ -z "${TARGET_VERSION}" ]]; then
        log_error ".opencode-version is empty"
        exit 1
    fi

    if ! [[ "${TARGET_VERSION}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
        log_error "Version '${TARGET_VERSION}' is not valid semver (major.minor.patch)"
        exit 1
    fi

    log_success "Target version: ${TARGET_VERSION}"
}

# ---------------------------------------------------------------------------
# STEP 2: Accept optional version argument
# ---------------------------------------------------------------------------
step_accept_version_arg() {
    if [[ $# -gt 0 ]]; then
        local arg_version="$1"
        if ! [[ "${arg_version}" =~ ^[0-9]+\.[0-9]+\.[0-9]+$ ]]; then
            log_error "Provided version '${arg_version}' is not valid semver"
            exit 1
        fi

        if [[ "${arg_version}" != "${TARGET_VERSION}" ]]; then
            log_info "Updating .opencode-version: ${TARGET_VERSION} → ${arg_version}"
            echo "${arg_version}" > "${VERSION_FILE}"
            TARGET_VERSION="${arg_version}"
            log_success ".opencode-version updated to ${TARGET_VERSION}"
        else
            log_info "Version argument matches .opencode-version, no update needed"
        fi
    fi

    NEXT_VERSION="$(bump_patch "${TARGET_VERSION}")"
    log_info "Release will produce v${TARGET_VERSION}, then bump to ${NEXT_VERSION}-SNAPSHOT"
}

# ---------------------------------------------------------------------------
# STEP 3: Rebuild Docker image
# ---------------------------------------------------------------------------
step_rebuild_docker() {
    log_info "=== STEP 3: Rebuild Docker image ==="

    if [[ ! -f "${DOCKER_DIR}/.env.opencode" ]]; then
        log_error "Required file not found: ${DOCKER_DIR}/.env.opencode"
        log_info "Create it from .env.opencode.example"
        exit 1
    fi

    # Copy env file so docker compose auto-loads it
    cp -f "${DOCKER_DIR}/.env.opencode" "${DOCKER_DIR}/.env"
    log_info "Copied .env.opencode → .env"

    cd "${DOCKER_DIR}"

    log_info "Stopping existing container..."
    docker compose down 2>/dev/null || true
    docker rm -f "${CONTAINER_NAME}" 2>/dev/null || true

    log_info "Building Docker image (no cache) with OPENCODE_VERSION=${TARGET_VERSION}..."
    OPENCODE_VERSION="${TARGET_VERSION}" docker compose build --no-cache

    # Tag compose-built image with the canonical name expected by TestContainers
    docker tag "$(cd "${DOCKER_DIR}" && docker compose config --format json 2>/dev/null | grep -oE '"image":"[^"]+"' | head -1 | sed 's/"image":"//;s/"//' 2>/dev/null || echo "opencode_opencode-server:${TARGET_VERSION}") "opencode-server:${TARGET_VERSION}" 2>/dev/null || true
    docker tag "opencode_opencode-server" "opencode-server:${TARGET_VERSION}" 2>/dev/null || true
    docker tag "opencode-opencode-server" "opencode-server:${TARGET_VERSION}" 2>/dev/null || true
    log_info "Tagged image as opencode-server:${TARGET_VERSION}"

    log_info "Starting container..."
    OPENCODE_VERSION="${TARGET_VERSION}" docker compose up -d

    cd "${PROJECT_ROOT}"
    log_success "Docker container started"
}

# ---------------------------------------------------------------------------
# STEP 4: Health check
# ---------------------------------------------------------------------------
step_health_check() {
    log_info "=== STEP 4: Wait for health check ==="

    local max_attempts=60
    local attempt=0

    log_info "Waiting for ${HEALTH_URL}..."

    while [[ ${attempt} -lt ${max_attempts} ]]; do
        if curl -sf -u "${OPENCODE_USERNAME}:${OPENCODE_PASSWORD}" "${HEALTH_URL}" >/dev/null 2>&1; then
            log_success "Server is healthy"
            return 0
        fi
        attempt=$((attempt + 1))
        log_info "Attempt ${attempt}/${max_attempts} — not ready, waiting 5s..."
        sleep 5
    done

    log_error "Server did not become healthy after ${max_attempts} attempts"
    log_info "=== Container logs ==="
    docker logs "${CONTAINER_NAME}" 2>&1 || true
    log_info "=== End of container logs ==="
    exit 1
}

# ---------------------------------------------------------------------------
# STEP 5: Verify installed version matches target
# ---------------------------------------------------------------------------
step_verify_version() {
    log_info "=== STEP 5: Verify installed OpenCode version ==="

    local installed_version

    # Try extracting from the container's package.json
    installed_version=$(docker exec "${CONTAINER_NAME}" sh -c \
        'cat /root/.config/opencode/package.json 2>/dev/null' \
        | grep -o '"@opencode-ai/plugin": *"[^"]*"' \
        | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' \
        | head -1 || true)

    if [[ -z "${installed_version}" ]]; then
        # Fallback: try opencode binary
        installed_version=$(docker exec "${CONTAINER_NAME}" opencode --version 2>/dev/null \
            | grep -oE '[0-9]+\.[0-9]+\.[0-9]+' | head -1 || true)
    fi

    if [[ -z "${installed_version}" ]]; then
        log_error "Could not extract OpenCode version from container"
        exit 1
    fi

    log_info "Installed version: ${installed_version}"
    log_info "Target version:    ${TARGET_VERSION}"

    if [[ "${installed_version}" != "${TARGET_VERSION}" ]]; then
        log_error "Version mismatch! Installed=${installed_version}, Target=${TARGET_VERSION}"
        log_error "Aborting release"
        exit 1
    fi

    log_success "Version matches: ${TARGET_VERSION}"
}

# ---------------------------------------------------------------------------
# STEP 6: Strip -SNAPSHOT from all pom.xml <revision> properties
# ---------------------------------------------------------------------------
step_strip_snapshot() {
    log_info "=== STEP 6: Strip -SNAPSHOT from pom.xml files ==="

    local clean_revision="${TARGET_VERSION}"

    for pom in "${POM_FILES[@]}"; do
        local current
        current="$(current_pom_revision "${pom}")"
        log_info "  ${pom}: <revision>${current}</revision>"

        if [[ "${current}" == "${clean_revision}" ]]; then
            log_info "    Already at clean version, skipping"
            continue
        fi

        set_pom_revision "${pom}" "${clean_revision}"

        local updated
        updated="$(current_pom_revision "${pom}")"
        if [[ "${updated}" != "${clean_revision}" ]]; then
            log_error "Failed to update ${pom} (got ${updated})"
            exit 1
        fi
        log_success "  Updated to <revision>${clean_revision}</revision>"
    done

    # Update <opencode.version> in examples/spring-boot/pom.xml
    local spring_pom="${PROJECT_ROOT}/examples/spring-boot/pom.xml"
    sed_inplace "s|<opencode.version>[^<]*</opencode.version>|<opencode.version>${clean_revision}</opencode.version>|" "${spring_pom}"
    log_success "  Updated <opencode.version> to ${clean_revision}"

    log_success "All pom.xml files set to ${clean_revision}"
}

# ---------------------------------------------------------------------------
# STEP 7: Full SDK build
# ---------------------------------------------------------------------------
step_build_sdk() {
    log_info "=== STEP 7: Build full SDK (mvn clean install) ==="

    cd "${PROJECT_ROOT}"

    if ! mvn clean install; then
        log_error "Maven build failed — aborting release"
        exit 1
    fi

    log_success "SDK built successfully"
}

# ---------------------------------------------------------------------------
# STEP 8: Run integration tests — hard stop on failure
# ---------------------------------------------------------------------------
step_integration_tests() {
    log_info "=== STEP 8: Run integration tests ==="

    cd "${PROJECT_ROOT}"

    if ! mvn verify -Pintegration-tests -pl examples/spring-boot -am; then
        log_error "Integration tests FAILED — rolling back pom.xml changes"

        # Rollback: restore -SNAPSHOT to all pom files
        for pom in "${POM_FILES[@]}"; do
            set_pom_revision "${pom}" "${TARGET_VERSION}-SNAPSHOT"
            log_warning "  Rolled back ${pom} to ${TARGET_VERSION}-SNAPSHOT"
        done

        log_error "Release aborted. No tag created, no version bump."
        exit 1
    fi

    log_success "Integration tests passed"
}

# ---------------------------------------------------------------------------
# STEP 9: Git commit clean version
# ---------------------------------------------------------------------------
step_git_commit_clean() {
    log_info "=== STEP 9: Git commit clean version ==="

    if git_has_clean_version_commit "${TARGET_VERSION}"; then
        log_info "Clean-version commit already exists, skipping"
        return 0
    fi

    cd "${PROJECT_ROOT}"
    git add -A
    git commit -m "Release v${TARGET_VERSION}" --allow-empty
    log_success "Committed clean version v${TARGET_VERSION}"
}

# ---------------------------------------------------------------------------
# STEP 10: Create git tag
# ---------------------------------------------------------------------------
step_git_tag() {
    log_info "=== STEP 10: Create git tag ==="

    if git_has_tag "${TARGET_VERSION}"; then
        log_warning "Tag v${TARGET_VERSION} already exists, skipping"
        return 0
    fi

    cd "${PROJECT_ROOT}"
    git tag "v${TARGET_VERSION}"
    log_success "Created tag v${TARGET_VERSION}"
}

# ---------------------------------------------------------------------------
# STEP 11: Bump .opencode-version to next patch
# ---------------------------------------------------------------------------
step_bump_version_file() {
    log_info "=== STEP 11: Bump .opencode-version to ${NEXT_VERSION} ==="

    echo "${NEXT_VERSION}" > "${VERSION_FILE}"
    log_success ".opencode-version updated to ${NEXT_VERSION}"
}

# ---------------------------------------------------------------------------
# STEP 12: Update all pom.xml <revision> to {next}-SNAPSHOT
# ---------------------------------------------------------------------------
step_set_snapshot() {
    log_info "=== STEP 12: Set pom.xml revisions to ${NEXT_VERSION}-SNAPSHOT ==="

    local snapshot_revision="${NEXT_VERSION}-SNAPSHOT"

    for pom in "${POM_FILES[@]}"; do
        set_pom_revision "${pom}" "${snapshot_revision}"

        local updated
        updated="$(current_pom_revision "${pom}")"
        if [[ "${updated}" != "${snapshot_revision}" ]]; then
            log_error "Failed to update ${pom} (got ${updated})"
            exit 1
        fi
        log_success "  ${pom}: <revision>${snapshot_revision}</revision>"
    done

    # Update <opencode.version> in examples/spring-boot/pom.xml
    local spring_pom="${PROJECT_ROOT}/examples/spring-boot/pom.xml"
    sed_inplace "s|<opencode.version>[^<]*</opencode.version>|<opencode.version>${NEXT_VERSION}</opencode.version>|" "${spring_pom}"
    log_success "  Updated <opencode.version> to ${NEXT_VERSION}"
}

# ---------------------------------------------------------------------------
# STEP 13: Git commit SNAPSHOT bump
# ---------------------------------------------------------------------------
step_git_commit_snapshot() {
    log_info "=== STEP 13: Git commit SNAPSHOT bump ==="

    if git_has_snapshot_commit "${NEXT_VERSION}"; then
        log_info "SNAPSHOT commit already exists, skipping"
        return 0
    fi

    cd "${PROJECT_ROOT}"
    git add -A
    git commit -m "Bump to ${NEXT_VERSION}-SNAPSHOT" --allow-empty
    log_success "Committed ${NEXT_VERSION}-SNAPSHOT"
}

# ---------------------------------------------------------------------------
# STEP 14: Print release summary
# ---------------------------------------------------------------------------
step_summary() {
    echo ""
    echo "========================================"
    log_success "Release pipeline completed!"
    echo "========================================"
    echo ""
    echo "  Released version:  v${TARGET_VERSION}"
    echo "  Next dev version:  ${NEXT_VERSION}-SNAPSHOT"
    echo "  Git tag:           v${TARGET_VERSION}"
    echo ""
    echo "  POM files updated:"
    for pom in "${POM_FILES[@]}"; do
        echo "    - $(realpath "${pom}" 2>/dev/null || echo "${pom}")"
    done
    echo ""
    echo "  NOTE: Changes are local only. Push when ready:"
    echo "    git push && git push --tags"
    echo ""
}

# ---------------------------------------------------------------------------
# Main
# ---------------------------------------------------------------------------
main() {
    echo "========================================"
    echo " OpenCode Java SDK — Release Pipeline"
    echo "========================================"
    echo ""

    detect_sed_inplace

    # Steps 1–2: Resolve target version
    step_read_version
    step_accept_version_arg "$@"

    # Steps 3–5: Docker infrastructure
    step_rebuild_docker
    step_health_check
    step_verify_version

    # Steps 6–8: Version updates, build, test
    step_strip_snapshot
    step_build_sdk
    step_integration_tests

    # Steps 9–10: Commit & tag
    step_git_commit_clean
    step_git_tag

    # Steps 11–13: Bump & commit SNAPSHOT
    step_bump_version_file
    step_set_snapshot
    step_git_commit_snapshot

    # Step 14: Summary
    step_summary
}

main "$@"
