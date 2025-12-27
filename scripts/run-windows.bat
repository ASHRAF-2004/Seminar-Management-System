@echo off
setlocal

rem Resolve repository root relative to this script so it can be double-clicked
for %%I in ("%~dp0..") do set "BASEDIR=%%~fI"
pushd "%BASEDIR%"

where javac >nul 2>&1
if errorlevel 1 goto :nojdk
where java >nul 2>&1
if errorlevel 1 goto :nojdk

if not exist out mkdir out
del /f /q sources.txt >nul 2>&1
dir /s /b "src\*.java" > sources.txt

javac -d out @sources.txt
if errorlevel 1 goto :cleanup

java -cp out ui.Main

:cleanup
set "EXITCODE=%ERRORLEVEL%"
del sources.txt >nul 2>&1
popd
endlocal & exit /b %EXITCODE%

:nojdk
echo JDK not found. Please install a Java Development Kit (JDK) and ensure "java" and "javac" are on your PATH. 1>&2
set "EXITCODE=1"
goto :cleanup
