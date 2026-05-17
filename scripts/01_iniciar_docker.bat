@echo off
echo ============================================
echo   Iniciando servicios Docker
echo   Mosquitto MQTT + RabbitMQ
echo ============================================

cd /d "%~dp0..\docker"
docker-compose up -d

echo.
echo Esperando que los servicios inicien...
timeout /t 10 /nobreak > nul

echo.
echo Verificando servicios...
docker ps

echo.
echo ============================================
echo   Servicios iniciados:
echo   - Mosquitto MQTT: localhost:1883
echo   - RabbitMQ AMQP:  localhost:5672
echo   - RabbitMQ Admin: http://localhost:15672
echo     (usuario: admin / clave: admin123)
echo ============================================
pause
