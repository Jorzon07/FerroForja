@echo off
title FerroForja - Backend
cd /d "%~dp0backend"
echo Iniciando FerroForja Backend en puerto 8080...
mvn spring-boot:run
pause
