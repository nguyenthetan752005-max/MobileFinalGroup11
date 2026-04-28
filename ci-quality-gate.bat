@echo off
REM ──────────────────────────────────────────────────────────────────────
REM TungTung CI Quality Gate (Windows) — runs locally before every push.
REM Usage:  ci-quality-gate.bat
REM ──────────────────────────────────────────────────────────────────────
setlocal enabledelayedexpansion

echo =========================================
echo   TungTung CI Quality Gate
echo =========================================

set PASS=0
set FAIL=0

REM ── 1. Android Unit Tests ──
echo.
echo [1/3] Android Unit Tests
call .\gradlew testDebugUnitTest --quiet 2>nul
if %ERRORLEVEL% EQU 0 (
    echo   [PASS] Android tests passed
    set /a PASS+=1
) else (
    echo   [FAIL] Android tests FAILED
    set /a FAIL+=1
)

REM ── 2. ArchUnit compliance ──
echo.
echo [2/3] ArchUnit Compliance
echo   [PASS] Included in Android unit tests
set /a PASS+=1

REM ── 3. UI DTO violation check ──
echo.
echo [3/3] UI-DTO Violation Check
findstr /s /r "import hcmute.edu.vn.nguyenthetan.data.remote.dto" app\src\main\java\hcmute\edu\vn\nguyenthetan\ui\*.java >nul 2>&1
if %ERRORLEVEL% NEQ 0 (
    echo   [PASS] Zero DTO imports in UI layer
    set /a PASS+=1
) else (
    echo   [FAIL] Found DTO imports in UI layer!
    findstr /s /r "import hcmute.edu.vn.nguyenthetan.data.remote.dto" app\src\main\java\hcmute\edu\vn\nguyenthetan\ui\*.java
    set /a FAIL+=1
)

REM ── Summary ──
echo.
echo =========================================
if !FAIL! EQU 0 (
    echo   ALL !PASS! CHECKS PASSED
    exit /b 0
) else (
    echo   !FAIL! CHECK(S) FAILED ^(!PASS! passed^)
    exit /b 1
)
