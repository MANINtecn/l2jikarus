package net.sf.l2jdev.loginserver;

import java.io.IOException;
import java.net.InetAddress;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.loginserver.config.LoginConfig;

public abstract class FloodProtectorListener extends Thread
{
	private static final Logger LOGGER = Logger.getLogger(FloodProtectorListener.class.getName());
	private final Map<String, FloodProtectorListener.ForeignConnection> _floodProtection = new ConcurrentHashMap<>();
	private final ServerSocket _serverSocket;
	
	public FloodProtectorListener(String listenIp, int port) throws IOException
	{
		if ("*".equals(listenIp))
		{
			this._serverSocket = new ServerSocket(port);
		}
		else
		{
			this._serverSocket = new ServerSocket(port, 50, InetAddress.getByName(listenIp));
		}
	}
	
	@Override
	public void run()
	{
		Socket clientSocket = null;
		
		while (!this.isInterrupted())
		{
			try
			{
				clientSocket = this._serverSocket.accept();
				if (LoginConfig.FLOOD_PROTECTION)
				{
					String clientAddress = clientSocket.getInetAddress().getHostAddress();
					FloodProtectorListener.ForeignConnection foreignConnection = this._floodProtection.get(clientAddress);
					if (foreignConnection == null)
					{
						foreignConnection = new FloodProtectorListener.ForeignConnection(System.currentTimeMillis());
						this._floodProtection.put(clientAddress, foreignConnection);
					}
					else
					{
						foreignConnection.connectionNumber++;
						if (foreignConnection.connectionNumber > LoginConfig.FAST_CONNECTION_LIMIT && System.currentTimeMillis() - foreignConnection.lastConnection < LoginConfig.NORMAL_CONNECTION_TIME || System.currentTimeMillis() - foreignConnection.lastConnection < LoginConfig.FAST_CONNECTION_TIME || foreignConnection.connectionNumber > LoginConfig.MAX_CONNECTION_PER_IP)
						{
							foreignConnection.lastConnection = System.currentTimeMillis();
							clientSocket.close();
							foreignConnection.connectionNumber--;
							if (!foreignConnection.isFlooding)
							{
								LOGGER.warning("Potential flood from address " + clientAddress + ".");
							}
							
							foreignConnection.isFlooding = true;
							continue;
						}
						
						if (foreignConnection.isFlooding)
						{
							foreignConnection.isFlooding = false;
							LOGGER.info("Address " + clientAddress + " is no longer considered as flooding.");
						}
						
						foreignConnection.lastConnection = System.currentTimeMillis();
					}
				}
				
				this.addClient(clientSocket);
			}
			catch (Exception var5)
			{
				if (this.isInterrupted())
				{
					try
					{
						this._serverSocket.close();
					}
					catch (IOException var4)
					{
						LOGGER.log(Level.INFO, "Error while closing server socket on interruption in " + this.getClass().getSimpleName() + ".", var4);
					}
					break;
				}
				
				LOGGER.log(Level.WARNING, "Unexpected exception in " + this.getClass().getSimpleName() + " main loop.", var5);
			}
		}
	}
	
	public abstract void addClient(Socket var1);
	
	public void removeFloodProtection(String ip)
	{
		if (LoginConfig.FLOOD_PROTECTION)
		{
			FloodProtectorListener.ForeignConnection foreignConnection = this._floodProtection.get(ip);
			if (foreignConnection != null)
			{
				foreignConnection.connectionNumber--;
				if (foreignConnection.connectionNumber == 0)
				{
					this._floodProtection.remove(ip);
				}
			}
			else
			{
				LOGGER.warning("Attempted to remove flood protection for unknown IP " + ip + ".");
			}
		}
	}
	
	public void close()
	{
		try
		{
			this._serverSocket.close();
		}
		catch (IOException var2)
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": " + var2.getMessage());
		}
	}
	
	protected static class ForeignConnection
	{
		public int connectionNumber;
		public long lastConnection;
		public boolean isFlooding = false;
		
		public ForeignConnection(long time)
		{
			this.lastConnection = time;
			this.connectionNumber = 1;
		}
	}
}
