package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class CustomMailManagerConfig
{
	public static final String CUSTOM_MAIL_MANAGER_CONFIG_FILE = "./config/Custom/CustomMailManager.ini";
	public static boolean CUSTOM_MAIL_MANAGER_ENABLED;
	public static int CUSTOM_MAIL_MANAGER_DELAY;

	public static void load()
	{
		ConfigReader custom = new ConfigReader("./config/Custom/CustomMailManager.ini");
		CUSTOM_MAIL_MANAGER_ENABLED = custom.getBoolean("CustomMailManagerEnabled", false);
		CUSTOM_MAIL_MANAGER_DELAY = custom.getInt("DatabaseQueryDelay", 30) * 1000;
	}
}
