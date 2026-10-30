@echo off
REM ============================================================================
REM  Compile a C++ source file with MSVC. No dev-shell setup needed.
REM  Usage:  run-cpp.cmd  <source.cpp> [extra sources/flags...]
REM  Output: bin\cpp\<name>.exe
REM
REM  Environment quirks worked around here:
REM   1. vcvars64.bat is NOT used - it calls reg.exe internally, which is
REM      blocked in this environment and aborts before cl.exe is ready.
REM      PATH/INCLUDE/LIB are assembled directly instead.
REM   2. Include paths are passed to cl.exe as explicit /I flags using the
REM      real long paths. The 8.3 short names resolve to the wrong directory
REM      here, and INCLUDE env vars get their separators mangled in transit.
REM
REM  ASCII-only on purpose: .cmd files are read in the OEM codepage (GBK here),
REM  non-ASCII comments get mangled and can break batch parsing.
REM ============================================================================

setlocal EnableExtensions

for %%I in ("%~dp0..\..") do set "ROOT=%%~fI"

if "%~1"=="" (
    echo [ERROR] No source file given.
    echo Usage: run-cpp.cmd ^<source.cpp^> [more args...]
    exit /b 1
)

set "SRCS=%~1"
shift
set "EXTRA=%1 %2 %3 %4 %5 %6 %7 %8 %9"

set "VCDIR=C:\Program Files\Microsoft Visual Studio\2022\Community"
set "SDKDIR=C:\Program Files (x86)\Windows Kits\10"

REM ---- Locate the newest MSVC toolset ----
set "MSVCVER="
for /f "delims=" %%D in ('dir /b /o-n "%VCDIR%\VC\Tools\MSVC" 2^>nul') do (
    if not defined MSVCVER set "MSVCVER=%%D"
)
if not defined MSVCVER (
    echo [ERROR] No MSVC toolset under "%VCDIR%\VC\Tools\MSVC".
    echo         Install VS 2022 "Desktop development with C++" workload.
    exit /b 1
)

REM ---- Locate the newest Windows SDK (10.0.x) ----
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
set "VCLIB=%VCDIR%\VC\Tools\MSVC\%MSVCVER%\lib\x64"
set "SDKROOT=%SDKDIR%\Include\%SDKVER%"

if not exist "%VCBIN%\cl.exe" (
    echo [ERROR] cl.exe not found at "%VCBIN%\cl.exe".
    exit /b 1
)

REM ---- Output dirs must exist before cl writes .obj/.exe into them ----
set "OUTDIR=%ROOT%\bin\cpp"
if not exist "%OUTDIR%" mkdir "%OUTDIR%"
if not exist "%ROOT%\bin\obj" mkdir "%ROOT%\bin\obj"

for %%F in ("%SRCS%") do set "NAME=%%~nF"

echo [INFO] MSVC %MSVCVER% / SDK %SDKVER%
echo [INFO] Compiling: %SRCS% %EXTRA%

set "PATH=%VCBIN%;%PATH%"
set "LIB=%VCLIB%;%SDKDIR%\Lib\%SDKVER%\ucrt\x64;%SDKDIR%\Lib\%SDKVER%\um\x64"

cl /nologo /EHsc /W3 /std:c++17 /O2 /utf-8 ^
 /I"%VCDIR%\VC\Tools\MSVC\%MSVCVER%\include" ^
 /I"%SDKROOT%\ucrt" /I"%SDKROOT%\um" /I"%SDKROOT%\shared" /I"%SDKROOT%\winrt" ^
 /Fe:"%OUTDIR%\%NAME%.exe" /Fo:"%ROOT%\bin\obj\\" "%SRCS%" %EXTRA%

if errorlevel 1 (
    echo [ERROR] Compilation failed.
    exit /b 1
)

if not exist "%OUTDIR%\%NAME%.exe" (
    echo [ERROR] Build reported success but %NAME%.exe was not produced.
    exit /b 1
)

echo [OK] Built: %OUTDIR%\%NAME%.exe
echo [INFO] Running...
echo ----------------------------------------
"%OUTDIR%\%NAME%.exe"
set "RC=%ERRORLEVEL%"
echo ----------------------------------------
if "%RC%"=="0" (echo [OK] Exited normally ^(exit 0^)) else (echo [WARN] Exit code: %RC%)

endlocal & exit /b %RC%