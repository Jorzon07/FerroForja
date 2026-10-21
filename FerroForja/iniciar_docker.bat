@echo off
title FerroForja - Docker Stack
cd /d "%~dp0"
echo ==============================================
echo   Iniciando Stack Docker de FerroForja (v2.1)
echo ==============================================
echo.
docker compose up -d --build
echo.
echo ==============================================
echo   Estado de los contenedores:
echo ==============================================
docker compose ps
echo.
echo - Frontend (Nginx): http://localhost (Puerto 80)
echo - Backend (API):    http://localhost:8080
echo - MySQL:            localhost:3306
echo.
pause
