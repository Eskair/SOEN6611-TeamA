@echo off
rem Builds and starts the iBank ABM. Run from the project folder: run.bat
call build.bat || exit /b 1
java -cp out ibank.Main
