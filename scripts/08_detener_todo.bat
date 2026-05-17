@echo off
echo ============================================
echo   Deteniendo todos los servicios
echo ============================================

echo Deteniendo contenedores Docker...
cd /d "%~dp0..\docker"
docker-compose down

echo.
echo Servicios Docker detenidos.
echo Para detener los scripts Python y Spring Boot,
echo cierre las ventanas de terminal correspondientes.
echo ============================================
pause
