@echo off
echo ============================================
echo   Iniciando Suscriptor MQTT (SQLite)
echo ============================================

cd /d "%~dp0..\mqtt-python\suscriptores"
python suscriptor_sqlite.py
pause
