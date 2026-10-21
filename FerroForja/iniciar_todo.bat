@echo off
title FerroForja - Lanzador
echo ==============================================
echo   Iniciando Sistema FerroForja (v2.1)
echo ==============================================
start "FerroForja Backend (8080)" cmd /k "cd /d %~dp0backend && mvn spring-boot:run"
timeout /t 5 /nobreak >nul
start "FerroForja Frontend (5173)" cmd /k "cd /d %~dp0frontend && npm.cmd run dev"
echo.
echo Los servicios se estan levantando en ventanas independientes:
echo - Backend:  http://localhost:8080
echo - Frontend: http://localhost:5173
echo.
pause
