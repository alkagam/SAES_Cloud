@echo off
echo Reestructurando proyecto MiniSaes por Actores...

:: 1. Crear carpetas de Actores y Recursos
if not exist "alumno" mkdir alumno
if not exist "docente" mkdir docente
if not exist "dae" mkdir dae
if not exist "gestion" mkdir gestion
if not exist "perfil" mkdir perfil
if not exist "sub" mkdir sub
if not exist "root" mkdir root
if not exist "docs" mkdir docs
if not exist "js" mkdir js
if not exist "css" mkdir css

:: 2. Mover archivos de Alumno (desde raiz, dashboards o pages)
move alumno-*.html alumno\ 2>nul
move dashboards\alumno-dashboard.html alumno\ 2>nul
move pages\alumno-*.html alumno\ 2>nul
:: Rescatar archivos mal ubicados
move js\alumno-*.html alumno\ 2>nul
move css\alumno-*.html alumno\ 2>nul

:: 3. Mover archivos de otros actores
move dashboard-docente.html docente\ 2>nul
move dashboards\dashboard-docente.html docente\ 2>nul
move dashboard-dae.html dae\ 2>nul
move dashboards\dashboard-dae.html dae\ 2>nul
move dashboard-gestion.html gestion\ 2>nul
move dashboards\dashboard-gestion.html gestion\ 2>nul
move dashboard-perfil.html perfil\ 2>nul
move dashboards\dashboard-perfil.html perfil\ 2>nul
move dashboard-sub.html sub\ 2>nul
move dashboards\dashboard-sub.html sub\ 2>nul
move dashboard-root.html root\ 2>nul
move dashboards\dashboard-root.html root\ 2>nul

:: 4. Mover Documentacion y Recursos
move permissions.html docs\ 2>nul
move pages\permissions.html docs\ 2>nul
move css\permissions.html docs\ 2>nul
move sidebar-loader.js js\ 2>nul
move global.css css\ 2>nul

:: 5. Limpiar carpetas viejas si estan vacias
rd dashboards 2>nul
rd pages 2>nul

echo Reestructuracion completada.
pause
