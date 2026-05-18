package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class PasswordChangeConfig
{
	public static final String PASSWORD_CHANGE_CONFIG_FILE = "./config/Custom/PasswordChange.ini";
	public static boolean ALLOW_CHANGE_PASSWORD;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/PasswordChange.ini");
		ALLOW_CHANGE_PASSWORD = config.getBoolean("AllowChangePassword", false);
	}
}
