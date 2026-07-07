@echo off
REM lab2\setup.bat - compile and run lab2.src.ExperimentMain (Windows)

setlocal

REM Directorio del script
set SCRIPT_DIR=%~dp0
cd /d "%SCRIPT_DIR%"

REM Directorio fuente y salida
set SRC_DIR=.
set OUT_DIR=out

REM Limpiar
if "%1"=="clean" (
    echo Cleaning %OUT_DIR%...
    if exist "%OUT_DIR%" rmdir /s /q "%OUT_DIR%"
    echo Done.
    endlocal
    exit /b 0
)

REM Crear carpeta de salida
if not exist "%OUT_DIR%" mkdir "%OUT_DIR%"

echo Compiling Java sources...

dir /b /s "%SRC_DIR%\*.java" > "%OUT_DIR%\sources.txt"

for %%I in ("%OUT_DIR%\sources.txt") do if %%~zI==0 (
    echo No Java sources found.
    del /q "%OUT_DIR%\sources.txt" >nul 2>&1
    endlocal
    exit /b 1
)

javac -d "%OUT_DIR%" @"%OUT_DIR%\sources.txt"

if errorlevel 1 (
    echo Compilation failed.
    del /q "%OUT_DIR%\sources.txt" >nul 2>&1
    endlocal
    exit /b 1
)

del /q "%OUT_DIR%\sources.txt" >nul 2>&1

echo.
echo Running ExperimentMain...
echo.

java -cp "%OUT_DIR%" lab2.Main

endlocal
exit /b %ERRORLEVEL%