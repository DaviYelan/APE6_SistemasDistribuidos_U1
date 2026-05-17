@echo off
echo ============================================
echo   Iniciando Servicio Puente MQTT - RabbitMQ
echo ============================================

cd /d "%~dp0..\mqtt-python\puente"
python bridge_mqtt_rabbitmq.py
pause
