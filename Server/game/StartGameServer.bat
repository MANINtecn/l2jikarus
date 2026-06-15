@echo off
title L2 SAMURAI CROW - Game Server
cd /d "%~dp0"
:start
echo [%date% %time%] Iniciando Game Server...
java -Duser.timezone=Etc/GMT+3 -Djava.awt.headless=true -Xmx8000m -cp "..\libs\*" net.sf.l2jdev.gameserver.GameServer
if %errorlevel% == 2 (
    echo Reiniciando...
    goto start
)
echo Servidor encerrado. Pressione qualquer tecla para fechar.
pause >nul