@echo off
echo ============================================
echo   Probando API REST - Fleet Monitor
echo ============================================

echo.
echo [1] GET /api/fleet/status
echo ----------------------------------------
curl -s http://localhost:8080/api/fleet/status | python -m json.tool 2>nul || curl -s http://localhost:8080/api/fleet/status
echo.

echo [2] GET /api/fleet/vehicles
echo ----------------------------------------
curl -s http://localhost:8080/api/fleet/vehicles | python -m json.tool 2>nul || curl -s http://localhost:8080/api/fleet/vehicles
echo.

echo [3] GET /api/fleet/vehicle/VH-001/telemetria
echo ----------------------------------------
curl -s http://localhost:8080/api/fleet/vehicle/VH-001/telemetria | python -m json.tool 2>nul || curl -s http://localhost:8080/api/fleet/vehicle/VH-001/telemetria
echo.

echo [4] GET /api/fleet/alerts
echo ----------------------------------------
curl -s http://localhost:8080/api/fleet/alerts | python -m json.tool 2>nul || curl -s http://localhost:8080/api/fleet/alerts
echo.

echo [5] GET /api/fleet/notifications
echo ----------------------------------------
curl -s http://localhost:8080/api/fleet/notifications | python -m json.tool 2>nul || curl -s http://localhost:8080/api/fleet/notifications
echo.

echo ============================================
echo   Pruebas completadas
echo ============================================
pause
