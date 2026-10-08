@echo off
setlocal EnableExtensions
cd /d "%~dp0"
if not exist "%~dp0logs" mkdir "%~dp0logs"
set "LOG=%~dp0logs\build-one-diagnose-latest.log"

> "%LOG%" echo Build-One diagnosis
>>"%LOG%" echo Date=%date% %time%
>>"%LOG%" echo BAT=%~f0
>>"%LOG%" echo CD=%CD%
>>"%LOG%" echo CMDCMDLINE=%CMDCMDLINE%
>>"%LOG%" echo COMSPEC=%COMSPEC%
>>"%LOG%" echo SESSIONNAME=%SESSIONNAME%
>>"%LOG%" echo WT_SESSION=%WT_SESSION%
>>"%LOG%" echo TERM_PROGRAM=%TERM_PROGRAM%
>>"%LOG%" echo Args=%*

echo ============================================================
echo Build-One launcher diagnosis
echo ============================================================
echo.
echo CMDCMDLINE:
echo %CMDCMDLINE%
echo.
echo Diagnostic log:
echo %LOG%
echo.
echo This test will now wait for you to press a key.
echo If this window closes without a keypress, the launcher/terminal itself
echo is closing it rather than our build script reaching an EXIT.
echo.
pause
>>"%LOG%" echo PAUSE returned at %date% %time%
echo.
echo PAUSE returned normally. Press another key to close.
pause >nul
exit /b 0
