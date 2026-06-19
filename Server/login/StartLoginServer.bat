@echo off
title L2 SAMURAI CROW - Login Server
cd /d "%~dp0"
:start
echo [%date% %time%] Iniciando Login Server...
java -Duser.timezone=Etc/GMT+3 -Djava.awt.headless=true -Xmx128m -cp "..\libs\*" net.sf.l2jdev.loginserver.LoginServer
if %errorlevel% == 2 (
    echo Reiniciando...
    goto start
)
echo Servidor encerrado. Pressione qualquer tecla para fechar.
pause >nul