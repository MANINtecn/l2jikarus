package net.sf.l2jdev.loginserver.config;

import java.io.File;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import net.sf.l2jdev.commons.util.ConfigReader;

public class LoginConfig
{
	private static final Logger LOGGER = Logger.getLogger(LoginConfig.class.getName());
	protected static final String SERVER_CONFIG_FILE = "./config/Server.ini";
	public static int GAME_SERVER_LOGIN_PORT;
	public static String GAME_SERVER_LOGIN_HOST;
	public static String LOGIN_BIND_ADDRESS;
	public static int PORT_LOGIN;
	public static File DATAPACK_ROOT;
	public static boolean ACCEPT_NEW_GAMESERVER;
	public static int LOGIN_TRY_BEFORE_BAN;
	public static int LOGIN_BLOCK_AFTER_BAN;
	public static boolean LOGIN_SERVER_SCHEDULE_RESTART;
	public static long LOGIN_SERVER_SCHEDULE_RESTART_TIME;
	public static boolean SHOW_LICENCE;
	public static boolean SHOW_PI_AGREEMENT;
	public static boolean AUTO_CREATE_ACCOUNTS;
	public static boolean FLOOD_PROTECTION;
	public static int FAST_CONNECTION_LIMIT;
	public static int NORMAL_CONNECTION_TIME;
	public static int FAST_CONNECTION_TIME;
	public static int MAX_CONNECTION_PER_IP;
	public static boolean ENABLE_CMD_LINE_LOGIN;
	public static boolean ONLY_CMD_LINE_LOGIN;
	
	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Server.ini");
		GAME_SERVER_LOGIN_HOST = config.getString("LoginHostname", "127.0.0.1");
		GAME_SERVER_LOGIN_PORT = config.getInt("LoginPort", 9013);
		LOGIN_BIND_ADDRESS = config.getString("LoginserverHostname", "0.0.0.0");
		PORT_LOGIN = config.getInt("LoginserverPort", 2106);
		
		try
		{
			DATAPACK_ROOT = new File(config.getString("DatapackRoot", ".").replaceAll("\\\\", "/")).getCanonicalFile();
		}
		catch (IOException var2)
		{
			LOGGER.log(Level.WARNING, "Error setting datapack root!", var2);
			DATAPACK_ROOT = new File(".");
		}
		
		ACCEPT_NEW_GAMESERVER = config.getBoolean("AcceptNewGameServer", true);
		LOGIN_TRY_BEFORE_BAN = config.getInt("LoginTryBeforeBan", 5);
		LOGIN_BLOCK_AFTER_BAN = config.getInt("LoginBlockAfterBan", 900);
		LOGIN_SERVER_SCHEDULE_RESTART = config.getBoolean("LoginRestartSchedule", false);
		LOGIN_SERVER_SCHEDULE_RESTART_TIME = config.getLong("LoginRestartTime", 24L);
		SHOW_LICENCE = config.getBoolean("ShowLicence", true);
		SHOW_PI_AGREEMENT = config.getBoolean("ShowPIAgreement", false);
		AUTO_CREATE_ACCOUNTS = config.getBoolean("AutoCreateAccounts", true);
		FLOOD_PROTECTION = config.getBoolean("EnableFloodProtection", true);
		FAST_CONNECTION_LIMIT = config.getInt("FastConnectionLimit", 15);
		NORMAL_CONNECTION_TIME = config.getInt("NormalConnectionTime", 700);
		FAST_CONNECTION_TIME = config.getInt("FastConnectionTime", 350);
		MAX_CONNECTION_PER_IP = config.getInt("MaxConnectionPerIP", 50);
		ENABLE_CMD_LINE_LOGIN = config.getBoolean("EnableCmdLineLogin", false);
		ONLY_CMD_LINE_LOGIN = config.getBoolean("OnlyCmdLineLogin", false);
	}
}
