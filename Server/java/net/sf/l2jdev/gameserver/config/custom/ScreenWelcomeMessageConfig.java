package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class ScreenWelcomeMessageConfig
{
	public static final String SCREEN_WELCOME_MESSAGE_CONFIG_FILE = "./config/Custom/ScreenWelcomeMessage.ini";
	public static boolean WELCOME_MESSAGE_ENABLED;
	public static String WELCOME_MESSAGE_TEXT;
	public static int WELCOME_MESSAGE_TIME;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/ScreenWelcomeMessage.ini");
		WELCOME_MESSAGE_ENABLED = config.getBoolean("ScreenWelcomeMessageEnable", false);
		WELCOME_MESSAGE_TEXT = config.getString("ScreenWelcomeMessageText", "Welcome to our server!");
		WELCOME_MESSAGE_TIME = config.getInt("ScreenWelcomeMessageTime", 10) * 1000;
	}
}
