@echo off
setlocal

cd /d "%~dp0"
title RPGConsole
chcp 65001 >nul

"application\RPGConsole.exe" %*

set "GAME_EXIT_CODE=%ERRORLEVEL%"

if not "%GAME_EXIT_CODE%"=="0" (
    echo.
    echo O RPGConsole foi encerrado com um erro.
    echo Codigo de saida: %GAME_EXIT_CODE%
    pause
)

endlocal & exit /b %GAME_EXIT_CODE%