@echo off
setlocal EnableExtensions
cd /d "%~dp0"

if "%~1"=="" (
    echo ============================================================
    echo Jukebox 360 - BUILD ONE
    echo ============================================================
    echo Available targets:
    powershell -NoProfile -ExecutionPolicy Bypass -Command "$cfg = Get-Content -Raw '%~dp0targets.json' | ConvertFrom-Json; $cfg.psobject.Properties.Name | ForEach-Object { Write-Host ('  ' + $_) }"
    echo.
    set /p "TARGET=Type the target to build (example: forge-1.19.2): "
    if not defined TARGET (
        echo No target entered. Nothing was built.
        echo.
        pause
        exit /b 1
    )
) else (
    set "TARGET=%~1"
)

if not exist "%~dp0logs" mkdir "%~dp0logs"
for /f %%I in ('powershell -NoProfile -Command "Get-Date -Format yyyyMMdd-HHmmss"') do set "STAMP=%%I"
set "LOG=%~dp0logs\build-%TARGET%-%STAMP%.log"
set "LATEST=%~dp0logs\build-one-latest.log"

echo.
echo ============================================================
echo Jukebox 360 - BUILD ONE: %TARGET%
echo ============================================================
echo Live output is enabled.
echo Log: %LOG%
echo Gradle distributions: C:\Gradle
echo ============================================================
echo.

powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build.ps1" "%TARGET%" -LogPath "%LOG%" %2 %3 %4
set "EXITCODE=%ERRORLEVEL%"
copy /Y "%LOG%" "%LATEST%" >nul 2>nul

echo.
echo ============================================================
if "%EXITCODE%"=="0" (
    echo BUILD FINISHED SUCCESSFULLY.
) else (
    echo BUILD FAILED. Exit code: %EXITCODE%
    echo Please send me this log file:
    echo   %LATEST%
)
echo ============================================================
echo.
pause
exit /b %EXITCODE%
