@echo off
setlocal EnableExtensions
cd /d "%~dp0"

if not exist "%~dp0logs" mkdir "%~dp0logs"
set "WRAPPERLOG=%~dp0logs\build-one-wrapper-latest.log"

rem -----------------------------------------------------------------
rem Explorer normally launches a .bat through: cmd.exe /c "Build-One.bat"
rem A plain PAUSE should work there, and did in the old single-select file,
rem but on this setup the multi-select wrapper's console is disappearing.
rem
rem When we detect that /C launch, replace it with an inner CMD /K session.
rem /K is a shell-level guarantee that the console remains open after the
rem batch returns; it does not depend on PAUSE, CHOICE, or stdin state.
rem
rem J360_BUILDONE_INNER prevents recursion.
rem -----------------------------------------------------------------
if not defined J360_BUILDONE_INNER (
    echo [%date% %time%] OUTER START>>"%WRAPPERLOG%"
    echo CMDCMDLINE=%CMDCMDLINE%>>"%WRAPPERLOG%"
    echo BAT=%~f0>>"%WRAPPERLOG%"
    echo ARGS=%*>>"%WRAPPERLOG%"

    echo(%CMDCMDLINE%| findstr /I /C:" /c " >nul
    if not errorlevel 1 (
        echo [%date% %time%] Explorer-/C launch detected; switching to persistent CMD /K.>>"%WRAPPERLOG%"
        set "J360_BUILDONE_INNER=1"
        cmd.exe /d /k call "%~f0" %*
        set "OUTEREXIT=%ERRORLEVEL%"
        echo [%date% %time%] Nested CMD /K finally exited. code=%OUTEREXIT%>>"%WRAPPERLOG%"
        exit /b %OUTEREXIT%
    )
)

echo [%date% %time%] INNER/NO-WRAP START>>"%WRAPPERLOG%"
echo CMDCMDLINE=%CMDCMDLINE%>>"%WRAPPERLOG%"

echo ============================================================
echo Jukebox 360 - BUILD SELECTED
echo ============================================================
echo.
echo   A) Fabric   1.20.1
echo   B) Fabric   1.21.1
echo   C) Fabric   1.21.10
echo   D) Fabric   26.1.x
echo   E) Fabric   26.2.x
echo   F) Fabric   26.3.x
echo   G) Forge    1.19.2
echo   H) Forge    1.20.1
echo   I) NeoForge 1.21.1
echo   J) NeoForge 26.1.x
echo   K) NeoForge 26.2.x
echo   L) NeoForge 26.3.x
echo.
echo Enter one letter for one build, or multiple letters separated by commas.
echo Example: F
echo Example: D,E,F,J,K,L
echo.

if "%~1"=="" (
    set /p "SELECTION=Build target(s): "
    if not defined SELECTION (
        echo.
        echo No target entered. Nothing was built.
        echo [%date% %time%] No selection; stopping.>>"%WRAPPERLOG%"
        echo.
        pause
        exit /b 1
    )
) else (
    set "SELECTION=%~1"
)

set "CLEANARG="
if /I "%~2"=="-Clean" set "CLEANARG=-Clean"

echo [%date% %time%] Selection=%SELECTION% Clean=%CLEANARG%>>"%WRAPPERLOG%"
echo [%date% %time%] Launching build-selected.ps1>>"%WRAPPERLOG%"

echo.
if defined CLEANARG (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-selected.ps1" "%SELECTION%" -Clean
) else (
    powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0scripts\build-selected.ps1" "%SELECTION%"
)

set "EXITCODE=%ERRORLEVEL%"
echo [%date% %time%] build-selected.ps1 returned code=%EXITCODE%>>"%WRAPPERLOG%"

echo.
echo ============================================================
if "%EXITCODE%"=="0" (
    echo BUILD-ONE FINISHED SUCCESSFULLY.
) else (
    echo BUILD-ONE FINISHED WITH ERROR(S). Exit code: %EXITCODE%
    echo Check the logs folder for the failing target log.
)
echo ============================================================
echo.
echo [%date% %time%] Reached final hold point.>>"%WRAPPERLOG%"

if defined J360_BUILDONE_INNER (
    echo This window is intentionally being kept open by CMD /K.
    echo You can close it normally when you're done checking the result.
    echo [%date% %time%] Returning to persistent CMD /K shell.>>"%WRAPPERLOG%"
    exit /b %EXITCODE%
)

rem Existing-terminal/manual launch fallback.
echo Press any key to close...
pause >nul
echo [%date% %time%] PAUSE returned; exiting with code=%EXITCODE%>>"%WRAPPERLOG%"
exit /b %EXITCODE%
