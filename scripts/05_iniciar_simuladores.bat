@echo off
echo ============================================
echo   Iniciando TODOS los simuladores IoT
echo ============================================

cd /d "%~dp0..\mqtt-python\simuladores"

echo Iniciando simulador GPS...
start "Simulador GPS" cmd /k "python simulador_gps.py"

timeout /t 2 /nobreak > nul

echo Iniciando simulador Temperatura...
start "Simulador Temperatura" cmd /k "python simulador_temperatura.py"

timeout /t 2 /nobreak > nul

echo Iniciando simulador Combustible...
start "Simulador Combustible" cmd /k "python simulador_combustible.py"

echo.
echo ============================================
echo   Simuladores iniciados en ventanas separadas
echo ============================================
pause
