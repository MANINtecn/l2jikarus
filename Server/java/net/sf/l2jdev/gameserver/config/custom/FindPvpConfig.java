package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class FindPvpConfig
{
	public static final String FIND_PVP_CONFIG_FILE = "./config/Custom/FindPvP.ini";
	public static boolean ENABLE_FIND_PVP;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/FindPvP.ini");
		ENABLE_FIND_PVP = config.getBoolean("EnableFindPvP", false);
	}
}
