package net.sf.l2jdev.loginserver;

import java.io.IOException;
import java.net.Socket;
import java.util.Collection;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.loginserver.config.LoginConfig;

public class GameServerListener extends FloodProtectorListener
{
	private static final Logger LOGGER = Logger.getLogger(GameServerListener.class.getName());
	private static final Collection<GameServerThread> _gameServerThreads = ConcurrentHashMap.newKeySet();
	
	public GameServerListener() throws IOException
	{
		super(LoginConfig.GAME_SERVER_LOGIN_HOST, LoginConfig.GAME_SERVER_LOGIN_PORT);
		this.setName(this.getClass().getSimpleName() + "-" + LoginConfig.GAME_SERVER_LOGIN_HOST + ":" + LoginConfig.GAME_SERVER_LOGIN_PORT);
	}
	
	@Override
	public void addClient(Socket socket)
	{
		if (socket != null && !socket.isClosed())
		{
			try
			{
				GameServerThread gameServerThread = new GameServerThread(socket);
				_gameServerThreads.add(gameServerThread);
				if (LOGGER.isLoggable(Level.FINE))
				{
					String address = socket.getInetAddress() != null ? socket.getInetAddress().getHostAddress() : "unknown";
					LOGGER.fine(this.getClass().getSimpleName() + ": Registered new game server connection from " + address + ". Active game servers: " + _gameServerThreads.size() + ".");
				}
			}
			catch (Exception var5)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed to register game server connection from " + socket.getRemoteSocketAddress() + ".", var5);
				
				try
				{
					socket.close();
				}
				catch (IOException var4)
				{
					LOGGER.log(Level.FINE, this.getClass().getSimpleName() + ": Failed to close socket after registration failure.", var4);
				}
			}
		}
		else
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Attempted to add game server with null or closed socket.");
		}
	}
	
	public void removeGameServer(GameServerThread gameServerThread)
	{
		if (gameServerThread == null)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Attempted to remove null game server thread.");
		}
		else
		{
			boolean removed = _gameServerThreads.remove(gameServerThread);
			if (LOGGER.isLoggable(Level.FINE))
			{
				LOGGER.fine(this.getClass().getSimpleName() + ": Removed game server thread " + gameServerThread + " (removed=" + removed + "). Active game servers: " + _gameServerThreads.size() + ".");
			}
		}
	}
	
	public static int getGameServerCount()
	{
		return _gameServerThreads.size();
	}
	
	public static Collection<GameServerThread> getGameServers()
	{
		return _gameServerThreads;
	}
}
