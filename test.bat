@echo off
rem Compiles iBank and its JUnit tests, then runs every test. Run from the project folder: test.bat
setlocal enabledelayedexpansion
call build.bat || exit /b 1
for %%j in (lib\junit-platform-console-standalone-*.jar) do set "JUNIT=%%j"
if exist out-test rmdir /s /q out-test
(for /r test %%f in (*.java) do (set "p=%%f" & echo "!p:\=/!")) > tests.txt
javac --release 17 -cp "out;%JUNIT%" -d out-test @tests.txt
if errorlevel 1 (del tests.txt & exit /b 1)
del tests.txt
java -jar "%JUNIT%" execute --class-path "out;out-test" --scan-class-path --details=tree
