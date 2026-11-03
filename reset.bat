@echo off
rem Starts iBank again from data\cardholders.csv and a full dispenser,
rem by deleting the state saved by previous runs. Run from the project folder: reset.bat
if exist data\state rmdir /s /q data\state
echo Saved state deleted: the next run.bat starts from the beginning.
