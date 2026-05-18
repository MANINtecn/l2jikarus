package net.sf.l2jdev.loginserver;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.LineNumberReader;
import java.net.InetSocketAddress;
import java.security.GeneralSecurityException;
import java.util.logging.Level;
import java.util.logging.LogManager;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.config.DatabaseConfig;
import net.sf.l2jdev.commons.config.InterfaceConfig;
import net.sf.l2jdev.commons.database.DatabaseBackup;
import net.sf.l2jdev.commons.database.DatabaseFactory;
import net.sf.l2jdev.commons.network.ConnectionManager;
import net.sf.l2jdev.commons.threads.ThreadPool;
import net.sf.l2jdev.loginserver.config.LoginConfig;
import net.sf.l2jdev.loginserver.network.LoginClient;
import net.sf.l2jdev.loginserver.network.LoginPacketHandler;
import net.sf.l2jdev.loginserver.ui.LoginServerLaucher;

public class LoginServer
{
	public static final Logger LOGGER = Logger.getLogger(LoginServer.class.getName());
	public static final int PROTOCOL_REV = 262;
	private static LoginServer _instance;
	private GameServerListener _gameServerListener;
	private static volatile int _loginStatus = 2;
	
	private LoginServer()
	{
		this.initializeInterfaceLayer();
		this.initializeLoggingLayer();
		this.initializeCoreServices();
		this.initializeSecurityAndBans();
		this.configureScheduledRestart();
		this.initializeNetworkListeners();
	}
	
	protected void initializeInterfaceLayer()
	{
		InterfaceConfig.load();
		if (InterfaceConfig.ENABLE_GUI)
		{
			System.out.println("LoginServer: Running in GUI mode.");
			new LoginServerLaucher();
		}
	}
	
	private void initializeLoggingLayer()
	{
		File logDirectory = new File(".", "log");
		if (!logDirectory.exists() && !logDirectory.mkdir())
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Unable to create log directory at " + logDirectory.getAbsolutePath() + ".");
		}
		
		File logConfigurationFile = new File("./log.cfg");
		if (!logConfigurationFile.exists())
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Logging configuration file is missing at " + logConfigurationFile.getAbsolutePath() + ". Using default logging settings.");
		}
		else if (logConfigurationFile.isDirectory())
		{
			LOGGER.warning(this.getClass().getSimpleName() + ": Logging configuration path points to a directory (" + logConfigurationFile.getAbsolutePath() + "). Using default logging settings.");
		}
		else
		{
			try (InputStream logConfigurationInputStream = new FileInputStream(logConfigurationFile))
			{
				LogManager.getLogManager().readConfiguration(logConfigurationInputStream);
			}
			catch (IOException var8)
			{
				LOGGER.log(Level.WARNING, this.getClass().getSimpleName() + ": Failed to apply logging configuration from " + logConfigurationFile.getAbsolutePath() + ". Reason: " + var8.getMessage(), var8);
			}
		}
	}
	
	protected void initializeCoreServices()
	{
		LoginConfig.load();
		DatabaseFactory.init();
		ThreadPool.init();
		
		try
		{
			LoginController.load();
		}
		catch (GeneralSecurityException var2)
		{
			LOGGER.log(Level.SEVERE, "FATAL: LoginController initialization failed due to security configuration error. Reason: " + var2.getMessage(), var2);
			System.exit(1);
		}
		
		GameServerTable.getInstance();
	}
	
	private void initializeSecurityAndBans()
	{
		this.loadBanFile();
	}
	
	private void configureScheduledRestart()
	{
		if (LoginConfig.LOGIN_SERVER_SCHEDULE_RESTART)
		{
			long restartDelayHours = LoginConfig.LOGIN_SERVER_SCHEDULE_RESTART_TIME;
			long restartDelayMillis = restartDelayHours * 3600000L;
			if (restartDelayMillis <= 0L)
			{
				LOGGER.warning("Login server restart scheduling is enabled but the computed delay is invalid. Configured hours: " + restartDelayHours + ".");
			}
			else
			{
				LOGGER.info("Login server restart scheduled in " + restartDelayHours + " hour(s). Computed delay: " + restartDelayMillis + " ms.");
				ThreadPool.schedule(() -> this.shutdown(true), restartDelayMillis);
			}
		}
	}
	
	private void initializeNetworkListeners()
	{
		this.startGameServerListener();
		this.startLoginClientListener();
	}
	
	private void startGameServerListener()
	{
		try
		{
			this._gameServerListener = new GameServerListener();
			this._gameServerListener.start();
			LOGGER.info("Game server listener is listening on " + LoginConfig.GAME_SERVER_LOGIN_HOST + ":" + LoginConfig.GAME_SERVER_LOGIN_PORT + ".");
		}
		catch (IOException var2)
		{
			LOGGER.log(Level.SEVERE, "FATAL: Game server listener could not be started on " + LoginConfig.GAME_SERVER_LOGIN_HOST + ":" + LoginConfig.GAME_SERVER_LOGIN_PORT + ". Reason: " + var2.getMessage(), var2);
			System.exit(1);
		}
	}
	
	private void startLoginClientListener()
	{
		try
		{
			new ConnectionManager<>(new InetSocketAddress(LoginConfig.LOGIN_BIND_ADDRESS, LoginConfig.PORT_LOGIN), LoginClient::new, new LoginPacketHandler());
			LOGGER.info(this.getClass().getSimpleName() + ": Login client listener started on " + LoginConfig.LOGIN_BIND_ADDRESS + ":" + LoginConfig.PORT_LOGIN + ".");
		}
		catch (IOException var2)
		{
			LOGGER.log(Level.SEVERE, "FATAL: Login client listener could not be started on " + LoginConfig.LOGIN_BIND_ADDRESS + ":" + LoginConfig.PORT_LOGIN + ". Reason: " + var2.getMessage(), var2);
			System.exit(1);
		}
	}
	
	public GameServerListener getGameServerListener()
	{
		return this._gameServerListener;
	}
	
	public void loadBanFile()
	{
		File bannedIpConfigurationFile = new File("./banned_ip.cfg");
		if (bannedIpConfigurationFile.exists() && bannedIpConfigurationFile.isFile())
		{
			String rawLine;
			try (FileInputStream bannedIpFileInputStream = new FileInputStream(bannedIpConfigurationFile); InputStreamReader bannedIpReader = new InputStreamReader(bannedIpFileInputStream); LineNumberReader bannedIpLineReader = new LineNumberReader(bannedIpReader);)
			{
				while ((rawLine = bannedIpLineReader.readLine()) != null)
				{
					this.processBanLine(bannedIpConfigurationFile, bannedIpLineReader, rawLine);
				}
			}
			catch (IOException var13)
			{
				LOGGER.log(Level.WARNING, "Error while reading IP bans configuration file (" + bannedIpConfigurationFile.getAbsolutePath() + "). Details: " + var13.getMessage(), var13);
			}
			
			LOGGER.info("Loaded " + LoginController.getInstance().getBannedIps().size() + " IP Bans.");
		}
		else
		{
			LOGGER.warning("IP bans configuration file (" + bannedIpConfigurationFile.getAbsolutePath() + ") does not exist or is not a regular file. No IP bans have been loaded.");
		}
	}
	
	protected void processBanLine(File sourceFile, LineNumberReader lineReader, String rawLine)
	{
		if (rawLine != null)
		{
			String trimmedLine = rawLine.trim();
			if (!trimmedLine.isEmpty())
			{
				if (trimmedLine.charAt(0) != '#')
				{
					int commentIndex = trimmedLine.indexOf(35);
					String definitionPart = commentIndex >= 0 ? trimmedLine.substring(0, commentIndex).trim() : trimmedLine;
					if (definitionPart.isEmpty())
					{
						LOGGER.warning("Skipped IP ban entry with empty definition in file (" + sourceFile.getAbsolutePath() + ") at line " + lineReader.getLineNumber() + ".");
					}
					else
					{
						String[] tokens = definitionPart.split("\\s+");
						if (tokens.length == 0)
						{
							LOGGER.warning("Skipped IP ban entry with no address token in file (" + sourceFile.getAbsolutePath() + ") at line " + lineReader.getLineNumber() + ".");
						}
						else
						{
							String ipAddress = tokens[0];
							long durationMillis = 0L;
							if (tokens.length > 1)
							{
								String durationToken = tokens[1];
								
								try
								{
									durationMillis = Long.parseLong(durationToken);
								}
								catch (NumberFormatException var14)
								{
									LOGGER.warning("Skipped IP ban entry due to invalid duration token '" + durationToken + "' in file (" + sourceFile.getAbsolutePath() + ") at line " + lineReader.getLineNumber() + ".");
									return;
								}
							}
							
							try
							{
								LoginController.getInstance().addBanForAddress(ipAddress, durationMillis);
							}
							catch (Exception var13)
							{
								LOGGER.warning("Skipped IP ban registration for address '" + ipAddress + "' from file (" + sourceFile.getAbsolutePath() + ") at line " + lineReader.getLineNumber() + ". Reason: " + var13.getMessage() + ".");
							}
						}
					}
				}
			}
		}
	}
	
	public void shutdown(boolean restart)
	{
		if (DatabaseConfig.BACKUP_DATABASE)
		{
			DatabaseBackup.performBackup("login");
		}
		
		Runtime.getRuntime().exit(restart ? 2 : 0);
	}
	
	public int getStatus()
	{
		return _loginStatus;
	}
	
	public void setStatus(int status)
	{
		_loginStatus = status;
	}
	
	public static LoginServer getInstance()
	{
		return _instance;
	}
	
	public static void main(String[] args)
	{
		_instance = new LoginServer();
	}
}
