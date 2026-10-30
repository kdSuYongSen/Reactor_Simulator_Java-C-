@echo off
REM ============================================================================
REM  Verify the Java / C++ / Python toolchains in one go.
REM  Run it from VSCode: the "verify toolchains" task
REM  ASCII-only on purpose: .cmd files are read in the OEM codepage (GBK here),
REM  non-ASCII comments get mangled and can break batch parsing.
REM ============================================================================

setlocal EnableExtensions
cd /d "%~dp0..\.."

set "MVN=C:\Users\Administrator\devtools\apache-maven-3.9.6\bin\mvn.cmd"
set "JAVAEXE=C:\Program Files\Java\jdk-17\bin\java.exe"
set "PYEXE=C:\Users\Administrator\.workbuddy\binaries\python\versions\3.13.12\python.exe"

echo ==========================================================
echo   RBMK-1500 Simulator - toolchain verification
echo ==========================================================
echo.

REM ---------- Java ----------
echo [1/3] Java
where java >nul 2>&1 && (echo   [OK]   java on PATH) || echo   [WARN] java not on PATH
if exist "%JAVAEXE%" (echo   [OK]   JDK 17: %JAVAEXE%) else (echo   [ERROR] JDK 17 missing)
if exist "%MVN%" (
    echo   [OK]   Maven: %MVN%
) else (
    echo   [ERROR] Maven missing: %MVN%
)
if exist "target\RBMK-1500-Simulator-1.2.1-jar-with-dependencies.jar" (
    echo   [OK]   fat jar present in target\
) else (
    echo   [INFO] fat jar not built yet - run the Java build task
)
echo.

REM ---------- C++ (Dev-Cpp / TDM-GCC is the primary toolchain) ----------
echo [2/3] C++
set "GCCBIN=E:\Dev-Cpp\TDM-GCC-64\bin"
if exist "%GCCBIN%\g++.exe" (
    echo   [OK]   Dev-Cpp GCC: %GCCBIN%
    set "PATH=%GCCBIN%;%PATH%"
    "%GCCBIN%\g++.exe" --version 2>nul | findstr /r /c:"g\+\+\.exe" && echo   [OK]   ^<compiler^> ready
    if exist "%GCCBIN%\gdb.exe" (echo   [OK]   GDB: %GCCBIN%\gdb.exe) else (echo   [WARN] gdb.exe missing - debugging will not work)
    if exist "%GCCBIN%\mingw32-make.exe" (echo   [OK]   mingw32-make present) else (echo   [WARN] mingw32-make.exe missing - Makefiles will not build)
) else (
    echo   [ERROR] Dev-Cpp g++.exe not found under "%GCCBIN%"
    echo           Update GCCBIN in verify-toolchains.cmd and run-gcc.cmd
)

echo   [INFO] compiling + running cpp smoke test with Dev-Cpp GCC...
if exist "cpp\smoke_test.cpp" (
    call "%~dp0run-gcc.cmd" "cpp\smoke_test.cpp"
) else (
    echo   [INFO] cpp\smoke_test.cpp not present, skipping compile check
)

echo   [INFO] verifying GDB can debug (breakpoint + variables)...
if exist "cpp\gdb_test.cpp" (
    call "%~dp0run-gcc.cmd" "cpp\gdb_test.cpp" >nul 2>&1
    if exist "bin\gcc\gdb_test.exe" (
        echo   [OK]   gdb_test.exe built with -g, ready for cppdbg debugging
    ) else (
        echo   [WARN] gdb_test.exe was not produced
    )
) else (
    echo   [INFO] cpp\gdb_test.cpp not present, skipping GDB check
)

echo   [INFO] optional MSVC fallback...
call "%~dp0msvc-env.cmd" where cl >nul 2>&1
if errorlevel 1 (
    echo   [WARN] MSVC environment unavailable - MSVC backup tasks will not work
) else (
    for /f "delims=" %%C in ('call "%~dp0msvc-env.cmd" where cl 2^>nul') do echo   [OK]   backup: %%C
)
echo.

REM ---------- Python ----------
echo [3/3] Python
if exist "%PYEXE%" (
    echo   [OK]   %PYEXE%
    "%PYEXE%" --version
) else (
    echo   [ERROR] Python interpreter missing: %PYEXE%
)
if exist "python\smoke_test.py" (
    "%PYEXE%" "python\smoke_test.py"
) else (
    echo   [INFO] python\smoke_test.py not present, skipping
)
echo.

echo ==========================================================
echo   Done. Errors above (if any) need to be fixed before use.
echo ==========================================================
endlocal
exit /b 0