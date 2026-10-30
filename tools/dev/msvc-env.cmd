@echo off
REM ============================================================================
REM  Activate the MSVC x64 build environment WITHOUT vcvars64.bat.
REM
REM  Usage:
REM    msvc-env.cmd                  -> opens a shell with cl.exe available
REM    msvc-env.cmd <command> [args] -> runs one command in that environment
REM
REM  Why not vcvars64.bat: it calls reg.exe internally, which is blocked in
REM  this environment, so it aborts before cl.exe is set up.
REM
REM  Why explicit long paths: the 8.3 short names (C:\PROGRA~1\...) resolve to
REM  the wrong directory here, and INCLUDE/LIB env vars come back with mixed
REM  separators. Long paths pass through correctly.
REM
REM  ASCII-only on purpose: .cmd files are read in the OEM codepage (GBK here),
REM  non-ASCII comments get mangled and can break batch parsing.
REM ============================================================================

setlocal EnableExtensions

set "VCDIR=C:\Program Files\Microsoft Visual Studio\2022\Community"
set "SDKDIR=C:\Program Files (x86)\Windows Kits\10"

set "MSVCVER="
for /f "delims=" %%D in ('dir /b /o-n "%VCDIR%\VC\Tools\MSVC" 2^>nul') do (
    if not defined MSVCVER set "MSVCVER=%%D"
)
if not defined MSVCVER (
    echo [ERROR] No MSVC toolset under "%VCDIR%\VC\Tools\MSVC".
    echo         Install VS 2022 "Desktop development with C++" workload.
    exit /b 1
)

set "SDKVER="
for /f "delims=" %%D in ('dir /b /o-n "%SDKDIR%\Include" 2^>nul') do (
    if not defined SDKVER (
        echo %%D | findstr /r /c:"^10\." >nul && set "SDKVER=%%D"
    )
)
if not defined SDKVER (
    echo [ERROR] No Windows SDK under "%SDKDIR%\Include".
    exit /b 1
)

set "VCBIN=%VCDIR%\VC\Tools\MSVC\%MSVCVER%\bin\HostX64\x64"

if not exist "%VCBIN%\cl.exe" (
    echo [ERROR] cl.exe not found at "%VCBIN%\cl.exe".
    exit /b 1
)

set "PATH=%VCBIN%;%SDKDIR%\bin\%SDKVER%\x64;%PATH%"
set "INCLUDE=%VCDIR%\VC\Tools\MSVC\%MSVCVER%\include;%SDKDIR%\Include\%SDKVER%\ucrt;%SDKDIR%\Include\%SDKVER%\um;%SDKDIR%\Include\%SDKVER%\shared;%SDKDIR%\Include\%SDKVER%\winrt"
set "LIB=%VCDIR%\VC\Tools\MSVC\%MSVCVER%\lib\x64;%SDKDIR%\Lib\%SDKVER%\ucrt\x64;%SDKDIR%\Lib\%SDKVER%\um\x64"

if not "%~1"=="" (
    %*
    exit /b %ERRORLEVEL%
)

echo [OK] MSVC x64 environment ready.
echo      MSVC toolset  : %MSVCVER%
echo      Windows SDK   : %SDKVER%
echo      cl.exe        : %VCBIN%\cl.exe
echo.
echo cl / nmake / link / cmake are now usable.
cmd /k
exit /b 0