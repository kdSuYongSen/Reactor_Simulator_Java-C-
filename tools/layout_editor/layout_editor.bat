@echo off
rem ============================================================
rem  RBMK-1500 Layout Editor  (launcher)
rem  ANY window, ANY nesting level: drag blocks or sub-components
rem  (panel -> sub-panel -> button/spinner...), auto-saves the
rem  layout json; the running Java app picks it up in ~1 second.
rem
rem  usage: layout_editor.bat [--out <layout file>]
rem                           [--page <page id>] [--drill <block/path>]
rem                           [--selftest]
rem ============================================================
setlocal
set "VENV_PY=%USERPROFILE%\.workbuddy\binaries\python\envs\default\Scripts\python.exe"
if exist "%VENV_PY%" goto run
where python >nul 2>nul
if %errorlevel%==0 (
  set "VENV_PY=python"
  goto run
)
echo [ERROR] No usable Python found.
pause
exit /b 1
:run
"%VENV_PY%" "%~dp0layout_editor.py" %*
if errorlevel 1 pause
endlocal
