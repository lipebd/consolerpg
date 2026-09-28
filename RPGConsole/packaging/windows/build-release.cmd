@echo off
setlocal EnableExtensions EnableDelayedExpansion

for %%I in ("%~dp0..\..") do set "PROJECT_ROOT=%%~fI"

set "VERSION=8.0.0"
set "APP_NAME=RPGConsole"
set "BUILD_DIR=%PROJECT_ROOT%\output\windows-v%VERSION%-build"
set "RELEASE_DIR=%PROJECT_ROOT%\output\%APP_NAME%-v%VERSION%"
set "ZIP_FILE=%PROJECT_ROOT%\output\%APP_NAME%-v%VERSION%-windows-x64.zip"
set "CLASSES_DIR=%BUILD_DIR%\classes"
set "INPUT_DIR=%BUILD_DIR%\input"
set "APP_IMAGE_DIR=%BUILD_DIR%\app-image"
set "SOURCES_FILE=%BUILD_DIR%\sources.txt"
set "JAR_FILE=%INPUT_DIR%\%APP_NAME%-v%VERSION%.jar"

echo Verificando ferramentas...

where javac >nul 2>&1
if errorlevel 1 (
    echo ERRO: javac nao foi encontrado.
    goto :error
)

where jar >nul 2>&1
if errorlevel 1 (
    echo ERRO: jar nao foi encontrado.
    goto :error
)

where jpackage >nul 2>&1
if errorlevel 1 (
    echo ERRO: jpackage nao foi encontrado.
    goto :error
)

echo Limpando o build anterior...

if exist "%BUILD_DIR%" rmdir /s /q "%BUILD_DIR%"
if exist "%RELEASE_DIR%" rmdir /s /q "%RELEASE_DIR%"
if exist "%ZIP_FILE%" del /q "%ZIP_FILE%"

mkdir "%CLASSES_DIR%"
mkdir "%INPUT_DIR%"
mkdir "%APP_IMAGE_DIR%"

echo Listando os arquivos Java...

> "%SOURCES_FILE%" (
    for /r "%PROJECT_ROOT%\src" %%F in (*.java) do (
        set "SOURCE_FILE=%%F"
        echo "!SOURCE_FILE:\=/!"
    )
)

echo Compilando em UTF-8...

javac -encoding UTF-8 -d "%CLASSES_DIR%" @"%SOURCES_FILE%"
if errorlevel 1 goto :error

echo Criando o JAR...

jar --create ^
    --file "%JAR_FILE%" ^
    --main-class rpg.Main ^
    -C "%CLASSES_DIR%" .

if errorlevel 1 goto :error

echo Criando o app-image...

jpackage ^
    --type app-image ^
    --name "%APP_NAME%" ^
    --app-version "%VERSION%" ^
    --input "%INPUT_DIR%" ^
    --main-jar "%APP_NAME%-v%VERSION%.jar" ^
    --main-class rpg.Main ^
    --dest "%APP_IMAGE_DIR%" ^
    --add-modules java.base ^
    --win-console

if errorlevel 1 goto :error

echo Montando a distribuicao...

mkdir "%RELEASE_DIR%"

xcopy "%APP_IMAGE_DIR%\%APP_NAME%" "%RELEASE_DIR%\application" /E /I /Y >nul
if errorlevel 1 goto :error

xcopy "%PROJECT_ROOT%\data" "%RELEASE_DIR%\data" /E /I /Y >nul
if errorlevel 1 goto :error

xcopy "%PROJECT_ROOT%\packaging\windows\fonts" "%RELEASE_DIR%\fonts" /E /I /Y >nul
if errorlevel 1 goto :error

copy /Y "%PROJECT_ROOT%\packaging\windows\RPGConsole.cmd" "%RELEASE_DIR%\" >nul
if errorlevel 1 goto :error

copy /Y "%PROJECT_ROOT%\packaging\windows\LEIA-ME.txt" "%RELEASE_DIR%\" >nul
if errorlevel 1 goto :error

echo Criando o ZIP...

powershell -NoProfile -Command ^
    "Compress-Archive -LiteralPath '%RELEASE_DIR%' -DestinationPath '%ZIP_FILE%' -CompressionLevel Optimal"

if errorlevel 1 goto :error

echo.
echo Build concluido:
echo %ZIP_FILE%
echo.
echo SHA-256:
certutil -hashfile "%ZIP_FILE%" SHA256

echo.
echo Teste a copia extraida antes de publicar.
exit /b 0

:error
echo.
echo O build da V8 falhou.
exit /b 1