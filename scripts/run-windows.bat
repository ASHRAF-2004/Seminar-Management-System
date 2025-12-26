@echo off
setlocal
if not exist out mkdir out
dir /s /b src\*.java > sources.txt
javac -d out @sources.txt
java -cp out ui.Main
del sources.txt >nul 2>&1
endlocal
