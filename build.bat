@echo off
rem Builds iBank with the plain JDK (no IDE needed). Run from the project folder: build.bat
setlocal enabledelayedexpansion
if exist out rmdir /s /q out
rem List the source files with forward slashes and quotes, so folders with spaces work.
(for /r src %%f in (*.java) do (set "p=%%f" & echo "!p:\=/!")) > sources.txt
javac --release 17 -d out @sources.txt
if errorlevel 1 (del sources.txt & exit /b 1)
del sources.txt
rem javac only compiles .java files, so the English/French text files are copied separately.
copy /y src\ibank\i18n\*.properties out\ibank\i18n\ > nul
echo Build OK: classes are in out\
