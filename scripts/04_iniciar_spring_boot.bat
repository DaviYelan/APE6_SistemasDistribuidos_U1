@echo off
echo ============================================
echo   Iniciando Fleet Monitor (Spring Boot)
echo ============================================

cd /d "%~dp0..\spring-boot-fleet"
mvnw.cmd spring-boot:run
pause
