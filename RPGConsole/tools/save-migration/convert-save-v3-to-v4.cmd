@echo off
setlocal EnableExtensions EnableDelayedExpansion

for %%I in ("%~dp0..\..") do set "PROJECT_ROOT=%%~fI"

set "BUILD_DIR=%PROJECT_ROOT%\output\save-migration-v3-to-v4"
set "CLASSES_DIR=%BUILD_DIR%\classes"
set "SOURCES_FILE=%BUILD_DIR%\sources.txt"
set "CONVERTER_SOURCE=%PROJECT_ROOT%\tools\save-migration\src\rpg\persistence\SaveV3ToV4Converter.java"

where javac >nul 2>&1
if errorlevel 1 (
    echo ERRO: javac nao foi encontrado.
    exit /b 1
)

if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"

mkdir "%CLASSES_DIR%"

> "%SOURCES_FILE%" (
    for /r "%PROJECT_ROOT%\src" %%F in (*.java) do (
        set "SOURCE_FILE=%%F"
        echo "!SOURCE_FILE:\=/!"
    )

    set "SOURCE_FILE=%CONVERTER_SOURCE%"
    echo "!SOURCE_FILE:\=/!"
)

javac -encoding UTF-8 -d "%CLASSES_DIR%" @"%SOURCES_FILE%"
if errorlevel 1 exit /b 1

java -cp "%CLASSES_DIR%" rpg.persistence.SaveV3ToV4Converter %*
exit /b %ERRORLEVEL%
