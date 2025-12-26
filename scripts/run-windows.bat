@echo off
setlocal

rem Resolve repository root relative to this script so it can be double-clicked
set "BASEDIR=%~dp0.."
pushd "%BASEDIR%"

if not exist out mkdir out
dir /s /b "src\*.java" > sources.txt

javac -d out @sources.txt
if errorlevel 1 goto :cleanup

java -cp out ui.Main

:cleanup
set "EXITCODE=%ERRORLEVEL%"
del sources.txt >nul 2>&1
popd
endlocal & exit /b %EXITCODE%
