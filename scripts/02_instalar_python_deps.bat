@echo off
echo ============================================
echo   Instalando dependencias Python
echo ============================================

cd /d "%~dp0..\mqtt-python"
pip install -r requirements.txt

echo.
echo Dependencias instaladas correctamente.
pause
