@echo off
setlocal enabledelayedexpansion

REM OpenCode Java SDK Release Script for Windows
REM Automates the release process: rebuild Docker, download OpenAPI, extract version, update POMs, build SDK

set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%"

REM Configuration
set "CONTAINER_NAME=opencode-server"
set "DOCKER_DIR=%PROJECT_ROOT%docker\opencode"
set "SDK_DIR=%PROJECT_ROOT%sdk"
if "%OPENCODE_SERVER_PORT%"=="" (
    set "OPENCODE_PORT=4096"
) else (
    set "OPENCODE_PORT=%OPENCODE_SERVER_PORT%"
)
if "%OPENCODE_SERVER_USERNAME%"=="" (
    set "OPENCODE_USERNAME=opencode"
) else (
    set "OPENCODE_USERNAME=%OPENCODE_SERVER_USERNAME%"
)
if "%OPENCODE_SERVER_PASSWORD%"=="" (
    set "OPENCODE_PASSWORD=opencode123"
) else (
    set "OPENCODE_PASSWORD=%OPENCODE_SERVER_PASSWORD%"
)
set "HEALTH_URL=http://localhost:%OPENCODE_PORT%/global/health"
set "OPENAPI_URL=http://localhost:%OPENCODE_PORT%/doc"

REM Colors are not natively supported in batch, using labels instead
goto :main

REM Logging functions
:log_info
echo [INFO] %~1
goto :eof

:log_success
echo [SUCCESS] %~1
goto :eof

:log_warning
echo [WARNING] %~1
goto :eof

:log_error
echo [ERROR] %~1
goto :eof

REM =============================================================================
REM STEP 1: Rebuild Docker Image and Start Container
REM =============================================================================

:rebuild_docker
call :log_info "=== STEP 1: Rebuild Docker Image and Start Container ==="

REM Check for .env.opencode file
if not exist "%DOCKER_DIR%\.env.opencode" (
    call :log_error "Required file not found: %DOCKER_DIR%\.env.opencode"
    call :log_info "Please create .env.opencode based on .env.opencode.example"
    exit /b 1
)

call :log_info "Found .env.opencode configuration file"

REM Copy .env.opencode to .env so docker compose can auto-load it
copy /Y "%DOCKER_DIR%\.env.opencode" "%DOCKER_DIR%\.env" >nul
call :log_info "Copied .env.opencode to .env"

REM Change to docker directory
cd /d "%DOCKER_DIR%"

REM Stop existing container if running
    call :log_info "Stopping existing container..."
    docker compose down >nul 2>&1

    REM Remove any existing containers with the same name (not just stop them)
    call :log_info "Removing any existing containers..."
    docker rm -f %CONTAINER_NAME% >nul 2>&1

    REM Rebuild image from scratch
call :log_info "Building Docker image (no cache)..."
docker compose build --no-cache
if errorlevel 1 (
    call :log_error "Docker build failed"
    exit /b 1
)

REM Start container
call :log_info "Starting Docker container..."
docker compose up -d
if errorlevel 1 (
    call :log_error "Failed to start Docker container"
    exit /b 1
)

REM Return to project root
cd /d "%PROJECT_ROOT%"

call :log_success "Docker container started successfully"
goto :eof

REM =============================================================================
REM STEP 2: Health Check & Download OpenAPI JSON
REM =============================================================================

:wait_for_healthy
call :log_info "=== STEP 2: Wait for Server Health Check ==="

set "max_attempts=60"
set "attempt=0"

call :log_info "Waiting for server at %HEALTH_URL%..."

:health_loop
set /a attempt+=1

curl -sf -u %OPENCODE_USERNAME%:%OPENCODE_PASSWORD% "%HEALTH_URL%" >nul 2>&1
if not errorlevel 1 (
    call :log_success "Server is healthy"
    goto :eof
)

if %attempt% geq %max_attempts% (
    call :log_error "Server failed to become healthy after %max_attempts% attempts"
    call :log_info "=== Docker Container Logs ==="
    docker logs %CONTAINER_NAME% 2>&1
    call :log_info "=== End of Docker Logs ==="
    exit /b 1
)

call :log_info "Attempt %attempt%/%max_attempts%: Server not ready yet, waiting 5 seconds..."
timeout /t 5 /nobreak >nul
goto :health_loop

goto :eof

:download_openapi
call :log_info "=== STEP 2b: Download OpenAPI JSON Spec ==="

call :log_info "Downloading OpenAPI spec from %OPENAPI_URL%..."

curl -sf -u %OPENCODE_USERNAME%:%OPENCODE_PASSWORD% "%OPENAPI_URL%" -o "%SDK_DIR%\openapi.json"
if errorlevel 1 (
    call :log_error "Failed to download OpenAPI spec"
    exit /b 1
)

REM Validate the downloaded file
if not exist "%SDK_DIR%\openapi.json" (
    call :log_error "Downloaded OpenAPI file not found"
    exit /b 1
)

for %%F in ("%SDK_DIR%\openapi.json") do set "file_size=%%~zF"
if %file_size% equ 0 (
    call :log_error "Downloaded OpenAPI file is empty"
    exit /b 1
)

call :log_success "OpenAPI spec downloaded successfully (%file_size% bytes)"
goto :eof

REM =============================================================================
REM STEP 3: Extract OpenCode Version from Container
REM =============================================================================

:extract_version
echo [INFO] === STEP 3: Extract OpenCode Version from Container ===

set "version_line="
set "EXTRACTED_VERSION="

REM Extract version from container's package.json
echo [INFO] Attempting to extract version from container's package.json...

for /f "delims=" %%a in ('docker exec %CONTAINER_NAME% cat /root/.config/opencode/package.json 2^>nul') do (
    echo %%a | findstr "@opencode-ai/plugin" >nul
    if !errorlevel! == 0 (
        set "version_line=%%a"
    )
)

if "!version_line!"=="" (
    echo [ERROR] Failed to extract version from /root/.config/opencode/package.json
    exit /b 1
)

echo [INFO] Found version line: !version_line!

REM Parse version from line like: "@opencode-ai/plugin": "1.4.3"
for /f "tokens=2 delims=:" %%a in ("!version_line!") do (
    set "raw_version=%%a"
    REM Trim leading/trailing whitespace and quotes
    for /f "tokens=* delims= " %%b in ("!raw_version!") do set "EXTRACTED_VERSION=%%b"
    set "EXTRACTED_VERSION=!EXTRACTED_VERSION:"=!"
)

if "!EXTRACTED_VERSION!"=="" (
    echo [ERROR] Could not parse version number from: !version_line!
    exit /b 1
)

echo [SUCCESS] Extracted OpenCode version: !EXTRACTED_VERSION!
goto :eof

REM =============================================================================
REM STEP 4: Update Version in pom.xml Files
REM =============================================================================

:update_version
echo [INFO] === STEP 4: Update Version in pom.xml Files ===

echo [INFO] Updating root pom.xml with version !EXTRACTED_VERSION!...

REM Create a temporary PowerShell script to handle the update
set "PS_SCRIPT=%TEMP%\update_pom_version.ps1"
(
echo $version = $env:EXTRACTED_VERSION
echo $pomPath = $env:POM_PATH
echo $content = Get-Content $pomPath -Raw
echo $content = $content -replace '&lt;revision&gt;[^&lt;]*&lt;/revision&gt;', "&lt;revision&gt;$version&lt;/revision&gt;"
echo $content ^| Set-Content $pomPath -NoNewline
) > "%PS_SCRIPT%"

REM Set environment variables for PowerShell
set "EXTRACTED_VERSION=!EXTRACTED_VERSION!"
set "POM_PATH=%PROJECT_ROOT%pom.xml"

REM Update root pom.xml
powershell -ExecutionPolicy Bypass -File "%PS_SCRIPT%"
if errorlevel 1 (
    del "%PS_SCRIPT%" 2>nul
    echo [ERROR] Failed to update root pom.xml
    exit /b 1
)

REM Verify the update
powershell -Command "if ((Get-Content '%PROJECT_ROOT%pom.xml' -Raw) -match '<revision>[^<]+</revision>') { exit 0 } else { exit 1 }"
if errorlevel 1 (
    del "%PS_SCRIPT%" 2>nul
    echo [ERROR] Failed to verify root pom.xml update
    exit /b 1
)
echo [SUCCESS] Root pom.xml updated successfully

REM Update examples/spring-boot/pom.xml
set "POM_PATH=%PROJECT_ROOT%examples\spring-boot\pom.xml"
powershell -ExecutionPolicy Bypass -File "%PS_SCRIPT%"
if errorlevel 1 (
    del "%PS_SCRIPT%" 2>nul
    echo [ERROR] Failed to update examples/spring-boot/pom.xml
    exit /b 1
)

powershell -Command "if ((Get-Content '%PROJECT_ROOT%examples\spring-boot\pom.xml' -Raw) -match '<revision>[^<]+</revision>') { exit 0 } else { exit 1 }"
if errorlevel 1 (
    del "%PS_SCRIPT%" 2>nul
    echo [ERROR] Failed to verify examples/spring-boot/pom.xml update
    exit /b 1
)
echo [SUCCESS] examples/spring-boot/pom.xml updated successfully

REM Clean up
del "%PS_SCRIPT%" 2>nul

echo [SUCCESS] All pom.xml files updated to version !EXTRACTED_VERSION!
goto :eof

REM =============================================================================
REM STEP 5: Build and Verify SDK
REM =============================================================================

:build_sdk
call :log_info "=== STEP 5: Build and Verify SDK ==="

cd /d "%PROJECT_ROOT%"

call :log_info "Running Maven build..."
call mvn clean install -pl sdk -am -q
if errorlevel 1 (
    call :log_error "Maven build failed"
    exit /b 1
)

call :log_success "SDK built successfully"
goto :eof

REM =============================================================================
REM Main Execution
REM =============================================================================

:main
echo ========================================
echo OpenCode Java SDK Release Script
echo ========================================
echo.

call :rebuild_docker
if errorlevel 1 goto :error

call :wait_for_healthy
if errorlevel 1 goto :error

call :download_openapi
if errorlevel 1 goto :error

call :extract_version
if errorlevel 1 goto :error

call :update_version
if errorlevel 1 goto :error

call :build_sdk
if errorlevel 1 goto :error

echo.
echo ========================================
call :log_success "Release process completed successfully!"
echo ========================================
echo.
echo Summary:
echo   - OpenCode version: %EXTRACTED_VERSION%
echo   - OpenAPI spec: sdk\openapi.json
echo   - pom.xml updated: %EXTRACTED_VERSION%
echo   - SDK build: Verified
echo.

goto :end

:error
echo.
call :log_error "Release process failed!"
exit /b 1

:end
endlocal
