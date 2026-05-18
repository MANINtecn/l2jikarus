package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class ServerTimeConfig
{
	public static final String SERVER_TIME_CONFIG_FILE = "./config/Custom/ServerTime.ini";
	public static boolean DISPLAY_SERVER_TIME;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/ServerTime.ini");
		DISPLAY_SERVER_TIME = config.getBoolean("DisplayServerTime", false);
	}
}
