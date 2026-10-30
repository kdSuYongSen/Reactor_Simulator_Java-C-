@echo off
REM ============================================================================
REM  Compile and run a C++ source file with the Dev-Cpp / TDM-GCC toolchain.
REM  Usage:  run-gcc.cmd  <source.cpp> [extra sources or flags...]
REM  Output: bin\gcc\<name>.exe
REM
REM  Toolchain: E:\Dev-Cpp\TDM-GCC-64  (GCC 9.2.0, GDB 8.3.1, Make 3.82)
REM
REM  ASCII-only on purpose: .cmd files are read in the OEM codepage (GBK here),
REM  non-ASCII comments get mangled and can break batch parsing.
REM ============================================================================

setlocal EnableExtensions

for %%I in ("%~dp0..\..") do set "ROOT=%%~fI"

if "%~1"=="" (
    echo [ERROR] No source file given.
    echo Usage: run-gcc.cmd ^<source.cpp^> [more args...]
    exit /b 1
)

set "SRCS=%~1"
shift
set "EXTRA=%1 %2 %3 %4 %5 %6 %7 %8 %9"

set "GCCPREFIX=E:\Dev-Cpp\TDM-GCC-64"
if not exist "%GCCPREFIX%\bin\g++.exe" (
    echo [ERROR] g++.exe not found under "%GCCPREFIX%\bin".
    echo         Edit GCCPREFIX at the top of this script to match your
    echo         Dev-Cpp installation, then run again.
    exit /b 1
)

REM The GCC runtime DLLs (libstdc++-6.dll, libgcc_s_seh_64-1.dll,
REM libwinpthread-1.dll) live next to g++.exe and must stay reachable
REM at run time, so put that bin dir on PATH.
set "PATH=%GCCPREFIX%\bin;%PATH%"

set "OUTDIR=%ROOT%\bin\gcc"
if not exist "%OUTDIR%" mkdir "%OUTDIR%"

for %%F in ("%SRCS%") do set "NAME=%%~nF"

echo [INFO] GCC toolchain: %GCCPREFIX%
echo [INFO] Compiling: %SRCS% %EXTRA%

REM -g emits DWARF debug info so the VSCode cppdbg config
REM (cppdbg + this gdb) can set breakpoints and inspect variables.
REM -static-libgcc/-static-libstdc++ keep the exe runnable without the
REM GCC runtime DLLs on PATH.
g++ -std=c++17 -Wall -Wextra -g -O0 -static-libgcc -static-libstdc++ -o "%OUTDIR%\%NAME%.exe" "%SRCS%" %EXTRA%

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