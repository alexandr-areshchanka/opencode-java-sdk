@echo off
setlocal enabledelayedexpansion

REM OpenCode Java SDK — Version-Aware Release Pipeline
REM
REM Usage: release.bat [version]
REM   If [version] is provided, .opencode-version is updated first.
REM   Otherwise the version is read from .opencode-version.
REM
REM Flow: rebuild Docker → verify version → strip SNAPSHOT → build → test →
REM       tag → bump → commit SNAPSHOT → summary
REM
REM Idempotent: safe to re-run if a step fails mid-way.

REM ---------------------------------------------------------------------------
REM Paths
REM ---------------------------------------------------------------------------
set "SCRIPT_DIR=%~dp0"
set "PROJECT_ROOT=%SCRIPT_DIR%"
set "VERSION_FILE=%PROJECT_ROOT%.opencode-version"
set "DOCKER_DIR=%PROJECT_ROOT%docker\opencode"

REM ---------------------------------------------------------------------------
REM Configuration
REM ---------------------------------------------------------------------------
set "CONTAINER_NAME=opencode-server"
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

REM ---------------------------------------------------------------------------
REM Jump to main (subroutines below)
REM ---------------------------------------------------------------------------
goto :main

REM ===========================================================================
REM Logging helpers (colored via ANSI escape sequences)
REM ===========================================================================

:log_info
for /f %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"
echo %ESC%[34m[INFO]%ESC%[0m %~1
goto :eof

:log_success
for /f %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"
echo %ESC%[32m[SUCCESS]%ESC%[0m %~1
goto :eof

:log_warning
for /f %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"
echo %ESC%[33m[WARNING]%ESC%[0m %~1
goto :eof

:log_error
for /f %%a in ('echo prompt $E ^| cmd') do set "ESC=%%a"
echo %ESC%[31m[ERROR]%ESC%[0m %~1
goto :eof

REM ===========================================================================
REM Platform helpers
REM ===========================================================================

:set_pom_revision
REM Usage: call :set_pom_revision <file> <new_revision>
set "POM_FILE=%~1"
set "NEW_REV=%~2"
set "POM_TEMP=%TEMP%\pom_update_%RANDOM%.xml"
powershell -Command "$c = Get-Content '%POM_FILE%' -Raw; $c = $c -replace '<revision>[^<]*</revision>', '<revision>%NEW_REV%</revision>'; $c | Set-Content '%POM_TEMP%' -NoNewline"
if errorlevel 1 (
    call :log_error "PowerShell failed updating %POM_FILE%"
    del "%POM_TEMP%" 2>nul
    exit /b 1
)
copy /y "%POM_TEMP%" "%POM_FILE%" >nul
del "%POM_TEMP%" 2>nul
goto :eof

:current_pom_revision
REM Usage: call :current_pom_revision <file>
REM Sets CURRENT_REV variable
set "REV_FILE=%~1"
set "CURRENT_REV="
for /f "delims=" %%r in ('powershell -Command "if ((Get-Content '%REV_FILE%' -Raw) -match '(?<=<revision>)[^<]+(?=</revision>)') { $matches[0] }"') do set "CURRENT_REV=%%r"
goto :eof

:bump_patch
REM Usage: call :bump_patch <version>
REM Sets BUMPED_VERSION variable
set "BUMP_INPUT=%~1"
set "BUMPED_VERSION="
for /f "delims=" %%v in ('powershell -Command "$parts = $env:BUMP_INPUT.Split('.'); $parts[2] = [int]$parts[2] + 1; $parts -join '.'"') do set "BUMPED_VERSION=%%v"
goto :eof

:validate_semver
REM Usage: call :validate_semver <version>
REM Sets IS_SEMVER=1 if valid, 0 if not
set "VER_INPUT=%~1"
set "IS_SEMVER=0"
for /f "delims=" %%r in ('powershell -Command "if ($env:VER_INPUT -match '^[0-9]+\.[0-9]+\.[0-9]+$') { '1' } else { '0' }"') do set "IS_SEMVER=%%r"
goto :eof

:git_has_clean_version_commit
REM Usage: call :git_has_clean_version_commit <version>
REM Sets HAS_COMMIT=1 if exists, 0 if not
set "GCV_VERSION=%~1"
set "HAS_COMMIT=0"
git log -1 --oneline --grep="Release v%GCV_VERSION%" HEAD >nul 2>&1
if not errorlevel 1 set "HAS_COMMIT=1"
goto :eof

:git_has_tag
REM Usage: call :git_has_tag <version>
REM Sets HAS_TAG=1 if exists, 0 if not
set "GT_VERSION=%~1"
set "HAS_TAG=0"
git rev-parse "v%GT_VERSION%" >nul 2>&1
if not errorlevel 1 set "HAS_TAG=1"
goto :eof

:git_has_snapshot_commit
REM Usage: call :git_has_snapshot_commit <version>
REM Sets HAS_SNAP_COMMIT=1 if exists, 0 if not
set "GSC_VERSION=%~1"
set "HAS_SNAP_COMMIT=0"
git log -1 --oneline --grep="Bump to %GSC_VERSION%-SNAPSHOT" HEAD >nul 2>&1
if not errorlevel 1 set "HAS_SNAP_COMMIT=1"
goto :eof

REM ===========================================================================
REM STEP 1: Read / set target version
REM ===========================================================================
:step_read_version
call :log_info "=== STEP 1: Read target version ==="

if not exist "%VERSION_FILE%" (
    call :log_error ".opencode-version file not found at %VERSION_FILE%"
    exit /b 1
)

set /p TARGET_VERSION=<%VERSION_FILE%

REM Trim whitespace
for /f "tokens=* delims= " %%a in ("%TARGET_VERSION%") do set "TARGET_VERSION=%%a"

if "%TARGET_VERSION%"=="" (
    call :log_error ".opencode-version is empty"
    exit /b 1
)

call :validate_semver "%TARGET_VERSION%"
if "%IS_SEMVER%"=="0" (
    call :log_error "Version '%TARGET_VERSION%' is not valid semver (major.minor.patch)"
    exit /b 1
)

call :log_success "Target version: %TARGET_VERSION%"
goto :eof

REM ===========================================================================
REM STEP 2: Accept optional version argument
REM ===========================================================================
:step_accept_version_arg
call :log_info "=== STEP 2: Accept optional version argument ==="

if not "%~1"=="" (
    set "ARG_VERSION=%~1"
    call :validate_semver "%ARG_VERSION%"
    if "%IS_SEMVER%"=="0" (
        call :log_error "Provided version '%ARG_VERSION%' is not valid semver"
        exit /b 1
    )

    if not "%ARG_VERSION%"=="%TARGET_VERSION%" (
        call :log_info "Updating .opencode-version: %TARGET_VERSION% =^> %ARG_VERSION%"
        echo %ARG_VERSION%> "%VERSION_FILE%"
        set "TARGET_VERSION=%ARG_VERSION%"
        call :log_success ".opencode-version updated to %TARGET_VERSION%"
    ) else (
        call :log_info "Version argument matches .opencode-version, no update needed"
    )
)

call :bump_patch "%TARGET_VERSION%"
set "NEXT_VERSION=%BUMPED_VERSION%"
call :log_info "Release will produce v%TARGET_VERSION%, then bump to %NEXT_VERSION%-SNAPSHOT"
goto :eof

REM ===========================================================================
REM STEP 3: Rebuild Docker image
REM ===========================================================================
:step_rebuild_docker
call :log_info "=== STEP 3: Rebuild Docker image ==="

if not exist "%DOCKER_DIR%\.env.opencode" (
    call :log_error "Required file not found: %DOCKER_DIR%\.env.opencode"
    call :log_info "Create it from .env.opencode.example"
    exit /b 1
)

REM Copy env file so docker compose auto-loads it
copy /y "%DOCKER_DIR%\.env.opencode" "%DOCKER_DIR%\.env" >nul
call :log_info "Copied .env.opencode to .env"

cd /d "%DOCKER_DIR%"

call :log_info "Stopping existing container..."
docker compose down >nul 2>&1
docker rm -f "%CONTAINER_NAME%" >nul 2>&1

call :log_info "Building Docker image (no cache) with OPENCODE_VERSION=%TARGET_VERSION%..."
set "OPENCODE_VERSION=%TARGET_VERSION%"
docker compose build --no-cache
if errorlevel 1 (
    cd /d "%PROJECT_ROOT%"
    call :log_error "Docker build failed"
    exit /b 1
)

REM Tag compose-built image with the canonical name expected by TestContainers
docker tag "opencode_opencode-server" "opencode-server:%TARGET_VERSION%" 2>nul
docker tag "opencode-opencode-server" "opencode-server:%TARGET_VERSION%" 2>nul
call :log_info "Tagged image as opencode-server:%TARGET_VERSION%"

call :log_info "Starting container..."
docker compose up -d
if errorlevel 1 (
    cd /d "%PROJECT_ROOT%"
    call :log_error "Failed to start Docker container"
    exit /b 1
)

cd /d "%PROJECT_ROOT%"
call :log_success "Docker container started"
goto :eof

REM ===========================================================================
REM STEP 4: Health check
REM ===========================================================================
:step_health_check
call :log_info "=== STEP 4: Wait for health check ==="

set "max_attempts=60"
set "attempt=0"

call :log_info "Waiting for %HEALTH_URL%..."

:health_loop
set /a attempt+=1

curl -sf -u "%OPENCODE_USERNAME%:%OPENCODE_PASSWORD%" "%HEALTH_URL%" >nul 2>&1
if not errorlevel 1 (
    call :log_success "Server is healthy"
    goto :eof
)

if %attempt% geq %max_attempts% (
    call :log_error "Server did not become healthy after %max_attempts% attempts"
    call :log_info "=== Container logs ==="
    docker logs "%CONTAINER_NAME%" 2>&1
    call :log_info "=== End of container logs ==="
    exit /b 1
)

call :log_info "Attempt %attempt%/%max_attempts% — not ready, waiting 5s..."
timeout /t 5 /nobreak >nul
goto :health_loop

REM ===========================================================================
REM STEP 5: Verify installed version matches target
REM ===========================================================================
:step_verify_version
call :log_info "=== STEP 5: Verify installed OpenCode version ==="

set "INSTALLED_VERSION="

REM Try extracting from the container's package.json
for /f "delims=" %%v in ('docker exec "%CONTAINER_NAME%" sh -c "cat /root/.config/opencode/package.json 2>/dev/null" 2^>nul ^| powershell -Command "$input | Select-String '\"@opencode-ai/plugin\":\s*\"([^\"]+)\"' | ForEach-Object { if ($_.Line -match '(\d+\.\d+\.\d+)') { $matches[1] } } | Select-Object -First 1"') do set "INSTALLED_VERSION=%%v"

if "%INSTALLED_VERSION%"=="" (
    REM Fallback: try opencode binary
    for /f "delims=" %%v in ('docker exec "%CONTAINER_NAME%" opencode --version 2^>nul ^| powershell -Command "$input | ForEach-Object { if ($_ -match '(\d+\.\d+\.\d+)') { $matches[1] } } | Select-Object -First 1"') do set "INSTALLED_VERSION=%%v"
)

if "%INSTALLED_VERSION%"=="" (
    call :log_error "Could not extract OpenCode version from container"
    exit /b 1
)

call :log_info "Installed version: %INSTALLED_VERSION%"
call :log_info "Target version:    %TARGET_VERSION%"

if not "%INSTALLED_VERSION%"=="%TARGET_VERSION%" (
    call :log_error "Version mismatch! Installed=%INSTALLED_VERSION%, Target=%TARGET_VERSION%"
    call :log_error "Aborting release"
    exit /b 1
)

call :log_success "Version matches: %TARGET_VERSION%"
goto :eof

REM ===========================================================================
REM STEP 6: Strip -SNAPSHOT from all pom.xml <revision> properties
REM ===========================================================================
:step_strip_snapshot
call :log_info "=== STEP 6: Strip -SNAPSHOT from pom.xml files ==="

set "CLEAN_REVISION=%TARGET_VERSION%"

REM --- Root pom.xml ---
call :current_pom_revision "%PROJECT_ROOT%pom.xml"
call :log_info "  pom.xml: <revision>!CURRENT_REV!</revision>"

if "!CURRENT_REV!"=="%CLEAN_REVISION%" (
    call :log_info "    Already at clean version, skipping"
) else (
    call :set_pom_revision "%PROJECT_ROOT%pom.xml" "%CLEAN_REVISION%"
    if errorlevel 1 exit /b 1
    call :current_pom_revision "%PROJECT_ROOT%pom.xml"
    if not "!CURRENT_REV!"=="%CLEAN_REVISION%" (
        call :log_error "Failed to update pom.xml (got !CURRENT_REV!)"
        exit /b 1
    )
    call :log_success "  Updated to <revision>%CLEAN_REVISION%</revision>"
)

REM --- examples/spring-boot/pom.xml ---
call :current_pom_revision "%PROJECT_ROOT%examples\spring-boot\pom.xml"
call :log_info "  examples\spring-boot\pom.xml: <revision>!CURRENT_REV!</revision>"

if "!CURRENT_REV!"=="%CLEAN_REVISION%" (
    call :log_info "    Already at clean version, skipping"
) else (
    call :set_pom_revision "%PROJECT_ROOT%examples\spring-boot\pom.xml" "%CLEAN_REVISION%"
    if errorlevel 1 exit /b 1
    call :current_pom_revision "%PROJECT_ROOT%examples\spring-boot\pom.xml"
    if not "!CURRENT_REV!"=="%CLEAN_REVISION%" (
        call :log_error "Failed to update examples\spring-boot\pom.xml (got !CURRENT_REV!)"
        exit /b 1
    )
call :log_success "  Updated to <revision>%CLEAN_REVISION%</revision>"
)

REM Update <opencode.version> in examples/spring-boot/pom.xml
set "SPRING_POM=%PROJECT_ROOT%examples\spring-boot\pom.xml"
powershell -Command "$c = Get-Content '%SPRING_POM%' -Raw; $c = $c -replace '<opencode.version>[^<]*</opencode.version>', '<opencode.version>%CLEAN_REVISION%</opencode.version>'; $c | Set-Content '%SPRING_POM%' -NoNewline"
call :log_success "  Updated <opencode.version> to %CLEAN_REVISION%"

call :log_success "All pom.xml files set to %CLEAN_REVISION%"
goto :eof

REM ===========================================================================
REM STEP 7: Full SDK build
REM ===========================================================================
:step_build_sdk
call :log_info "=== STEP 7: Build full SDK (mvn clean install) ==="

cd /d "%PROJECT_ROOT%"

call mvn clean install
if errorlevel 1 (
    call :log_error "Maven build failed — aborting release"
    exit /b 1
)

call :log_success "SDK built successfully"
goto :eof

REM ===========================================================================
REM STEP 8: Run integration tests — hard stop on failure
REM ===========================================================================
:step_integration_tests
call :log_info "=== STEP 8: Run integration tests ==="

cd /d "%PROJECT_ROOT%"

call mvn verify -Pintegration-tests -pl examples/spring-boot -am
if errorlevel 1 (
    call :log_error "Integration tests FAILED — rolling back pom.xml changes"

    REM Rollback: restore -SNAPSHOT to all pom files
    call :set_pom_revision "%PROJECT_ROOT%pom.xml" "%TARGET_VERSION%-SNAPSHOT"
    call :log_warning "  Rolled back pom.xml to %TARGET_VERSION%-SNAPSHOT"

    call :set_pom_revision "%PROJECT_ROOT%examples\spring-boot\pom.xml" "%TARGET_VERSION%-SNAPSHOT"
    call :log_warning "  Rolled back examples\spring-boot\pom.xml to %TARGET_VERSION%-SNAPSHOT"

    call :log_error "Release aborted. No tag created, no version bump."
    exit /b 1
)

call :log_success "Integration tests passed"
goto :eof

REM ===========================================================================
REM STEP 9: Git commit clean version
REM ===========================================================================
:step_git_commit_clean
call :log_info "=== STEP 9: Git commit clean version ==="

call :git_has_clean_version_commit "%TARGET_VERSION%"
if "%HAS_COMMIT%"=="1" (
    call :log_info "Clean-version commit already exists, skipping"
    goto :eof
)

cd /d "%PROJECT_ROOT%"
git add -A
git commit -m "Release v%TARGET_VERSION%" --allow-empty
call :log_success "Committed clean version v%TARGET_VERSION%"
goto :eof

REM ===========================================================================
REM STEP 10: Create git tag
REM ===========================================================================
:step_git_tag
call :log_info "=== STEP 10: Create git tag ==="

call :git_has_tag "%TARGET_VERSION%"
if "%HAS_TAG%"=="1" (
    call :log_warning "Tag v%TARGET_VERSION% already exists, skipping"
    goto :eof
)

cd /d "%PROJECT_ROOT%"
git tag "v%TARGET_VERSION%"
call :log_success "Created tag v%TARGET_VERSION%"
goto :eof

REM ===========================================================================
REM STEP 11: Bump .opencode-version to next patch
REM ===========================================================================
:step_bump_version_file
call :log_info "=== STEP 11: Bump .opencode-version to %NEXT_VERSION% ==="

echo %NEXT_VERSION%> "%VERSION_FILE%"
call :log_success ".opencode-version updated to %NEXT_VERSION%"
goto :eof

REM ===========================================================================
REM STEP 12: Update all pom.xml <revision> to {next}-SNAPSHOT
REM ===========================================================================
:step_set_snapshot
call :log_info "=== STEP 12: Set pom.xml revisions to %NEXT_VERSION%-SNAPSHOT ==="

set "SNAPSHOT_REVISION=%NEXT_VERSION%-SNAPSHOT"

REM --- Root pom.xml ---
call :set_pom_revision "%PROJECT_ROOT%pom.xml" "%SNAPSHOT_REVISION%"
if errorlevel 1 exit /b 1
call :current_pom_revision "%PROJECT_ROOT%pom.xml"
if not "!CURRENT_REV!"=="%SNAPSHOT_REVISION%" (
    call :log_error "Failed to update pom.xml (got !CURRENT_REV!)"
    exit /b 1
)
call :log_success "  pom.xml: <revision>%SNAPSHOT_REVISION%</revision>"

REM --- examples/spring-boot/pom.xml ---
call :set_pom_revision "%PROJECT_ROOT%examples\spring-boot\pom.xml" "%SNAPSHOT_REVISION%"
if errorlevel 1 exit /b 1
call :current_pom_revision "%PROJECT_ROOT%examples\spring-boot\pom.xml"
if not "!CURRENT_REV!"=="%SNAPSHOT_REVISION%" (
    call :log_error "Failed to update examples\spring-boot\pom.xml (got !CURRENT_REV!)"
    exit /b 1
)
call :log_success "  examples\spring-boot\pom.xml: <revision>%SNAPSHOT_REVISION%</revision>"

REM Update <opencode.version> in examples/spring-boot/pom.xml
set "SNAP_SPRING_POM=%PROJECT_ROOT%examples\spring-boot\pom.xml"
powershell -Command "$c = Get-Content '%SNAP_SPRING_POM%' -Raw; $c = $c -replace '<opencode.version>[^<]*</opencode.version>', '<opencode.version>%NEXT_VERSION%</opencode.version>'; $c | Set-Content '%SNAP_SPRING_POM%' -NoNewline"
call :log_success "  Updated <opencode.version> to %NEXT_VERSION%"

goto :eof

REM ===========================================================================
REM STEP 13: Git commit SNAPSHOT bump
REM ===========================================================================
:step_git_commit_snapshot
call :log_info "=== STEP 13: Git commit SNAPSHOT bump ==="

call :git_has_snapshot_commit "%NEXT_VERSION%"
if "%HAS_SNAP_COMMIT%"=="1" (
    call :log_info "SNAPSHOT commit already exists, skipping"
    goto :eof
)

cd /d "%PROJECT_ROOT%"
git add -A
git commit -m "Bump to %NEXT_VERSION%-SNAPSHOT" --allow-empty
call :log_success "Committed %NEXT_VERSION%-SNAPSHOT"
goto :eof

REM ===========================================================================
REM STEP 14: Print release summary
REM ===========================================================================
:step_summary
echo.
echo ========================================
call :log_success "Release pipeline completed!"
echo ========================================
echo.
echo   Released version:  v%TARGET_VERSION%
echo   Next dev version:  %NEXT_VERSION%-SNAPSHOT
echo   Git tag:           v%TARGET_VERSION%
echo.
echo   POM files updated:
echo     - %PROJECT_ROOT%pom.xml
echo     - %PROJECT_ROOT%examples\spring-boot\pom.xml
echo.
echo   NOTE: Changes are local only. Push when ready:
echo     git push ^&^& git push --tags
echo.
goto :eof

REM ===========================================================================
REM Main
REM ===========================================================================
:main
echo ========================================
echo  OpenCode Java SDK — Release Pipeline
echo ========================================
echo.

REM Steps 1-2: Resolve target version
call :step_read_version
if errorlevel 1 goto :error

call :step_accept_version_arg %*
if errorlevel 1 goto :error

REM Steps 3-5: Docker infrastructure
call :step_rebuild_docker
if errorlevel 1 goto :error

call :step_health_check
if errorlevel 1 goto :error

call :step_verify_version
if errorlevel 1 goto :error

REM Steps 6-8: Version updates, build, test
call :step_strip_snapshot
if errorlevel 1 goto :error

call :step_build_sdk
if errorlevel 1 goto :error

call :step_integration_tests
if errorlevel 1 goto :error

REM Steps 9-10: Commit & tag
call :step_git_commit_clean
if errorlevel 1 goto :error

call :step_git_tag
if errorlevel 1 goto :error

REM Steps 11-13: Bump & commit SNAPSHOT
call :step_bump_version_file
if errorlevel 1 goto :error

call :step_set_snapshot
if errorlevel 1 goto :error

call :step_git_commit_snapshot
if errorlevel 1 goto :error

REM Step 14: Summary
call :step_summary
goto :end

:error
echo.
call :log_error "Release process failed!"
cd /d "%PROJECT_ROOT%"
exit /b 1

:end
cd /d "%PROJECT_ROOT%"
endlocal
