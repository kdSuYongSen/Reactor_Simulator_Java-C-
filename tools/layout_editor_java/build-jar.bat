@echo off
setlocal
rem ===========================================================
rem  Build RBMK-LayoutEditor.jar (standalone layout editor)
rem  Requires JDK (javac/jar). org.json is lifted from the
rem  simulator fat jar and shaded into this jar, so the tool
rem  runs standalone (also against the original simulator).
rem ===========================================================
set HERE=%~dp0
cd /d "%HERE%"

set JDK=C:/Program Files\Java\jdk-24
if not exist "%JDK%\bin\javac.exe" set JDK=%JAVA_HOME%
if not exist "%JDK%\bin\javac.exe" (
  echo [ERR] JDK not found. Set JAVA_HOME or edit JDK= in this script.
  exit /b 1
)

set DEP=%HERE%..\..\target\RBMK-1500-Simulator-1.00Alpha-jar-with-dependencies.jar
if not exist "%DEP%" (
  echo [ERR] dependency jar not found: %DEP%
  echo       run "mvn package" once first ^(only org.json is needed^).
  exit /b 1
)

if exist build rmdir /s /q build
mkdir build\classes
pushd build\classes
"%JDK%\bin\jar.exe" xf "%DEP%" org/json
popd

echo [1/3] compiling...
rem --release 8: the simulator is also shipped with a 32-bit JRE 8, so the tool must run there too
"%JDK%\bin\javac.exe" --release 8 -encoding UTF-8 -nowarn -cp "build\classes" -d "build\classes" src\com\darwish\nppsim\layeditor\*.java
if errorlevel 1 (
  echo [ERR] compile failed
  exit /b 1
)

echo [2/3] packing jar...
> build\manifest.mf echo Main-Class: com.darwish.nppsim.layeditor.Main
if exist RBMK-LayoutEditor.jar del /q RBMK-LayoutEditor.jar
"%JDK%\bin\jar.exe" cfm RBMK-LayoutEditor.jar build\manifest.mf -C build\classes .

echo [3/3] copying next to the simulator...
copy /y RBMK-LayoutEditor.jar "%HERE%..\..\RBMK-LayoutEditor.jar" >nul
if exist "%HERE%..\..\target" copy /y RBMK-LayoutEditor.jar "%HERE%..\..\target\RBMK-LayoutEditor.jar" >nul

echo.
echo OK: %HERE%RBMK-LayoutEditor.jar
echo Run: java -jar RBMK-LayoutEditor.jar [layout-folder]
endlocal
