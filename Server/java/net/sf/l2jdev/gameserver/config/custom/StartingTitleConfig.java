package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class StartingTitleConfig
{
	public static final String STARTING_TITLE_CONFIG_FILE = "./config/Custom/StartingTitle.ini";
	public static boolean ENABLE_CUSTOM_STARTING_TITLE;
	public static String CUSTOM_STARTING_TITLE;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/StartingTitle.ini");
		ENABLE_CUSTOM_STARTING_TITLE = config.getBoolean("EnableStartingTitle", false);
		CUSTOM_STARTING_TITLE = config.getString("StartingTitle", "Newbie");
	}
}
