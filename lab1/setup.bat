@echo off
REM lab1\setup.bat - compile and run lab1.Main (Windows)
REM
REM Simple build script for the `lab1` project. Actions:
REM  - No args: compile all `.java` files under the current folder (recursively)
REM    into `out/` and run the `lab1.Main` class.
REM  - `clean`: remove the `out/` folder and exit.

REM Enable local environment changes for this script only
setlocal

REM SCRIPT_DIR: absolute path to the folder containing this script
set SCRIPT_DIR=%~dp0

REM Move to script folder so relative paths work reliably
cd /d "%SCRIPT_DIR%"

REM SRC_DIR: where to search for .java files ('.' = this folder and subfolders)
set SRC_DIR=.
REM OUT_DIR: where compiled .class files are placed
set OUT_DIR=out

REM Handle simple clean command: delete the `out` folder
if "%1"=="clean" (
  echo Cleaning %OUT_DIR%...
  if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
  echo Done.
  endlocal
  exit /b 0
)

REM Create output folder if needed and gather all Java sources
echo Compiling Java sources under "%SRC_DIR%" (recursively)...
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

REM Produce a list of all .java files into a temporary response file for javac
dir /b /s "%SRC_DIR%\*.java" > "%OUT_DIR%\sources.txt"

REM If no sources were found, inform and exit with error
for %%I in ("%OUT_DIR%\sources.txt") do if %%~zI==0 (
  echo No Java sources found under %SRC_DIR%.
  del /q "%OUT_DIR%\sources.txt" >nul 2>&1
  endlocal
  exit /b 1
)

REM Compile all sources. The @file syntax passes the file list to javac.
javac -d "%OUT_DIR%" @"%OUT_DIR%\sources.txt"
if errorlevel 1 (
  echo Compilation failed.
  del /q "%OUT_DIR%\sources.txt" >nul 2>&1
  endlocal
  exit /b 1
)

REM Cleanup the temporary sources list
del /q "%OUT_DIR%\sources.txt" >nul 2>&1

REM Run the main class (expects package `lab1` and class `Main`)
echo Compilation succeeded. Running lab1.Main...
java -cp "%OUT_DIR%" lab1.Main

REM Restore environment and return the Java process exit code
endlocal
exit /b %ERRORLEVEL%
