package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class OrcFortressConfig
{
	public static final String ORC_FORTRESS_CONFIG_FILE = "./config/OrcFortress.ini";
	public static boolean ORC_FORTRESS_ENABLE;
	public static int ORC_FORTRESS_HOUR;
	public static int ORC_FORTRESS_MINUTE;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/OrcFortress.ini");
		String[] orcFortressTime = config.getString("OrcFortressTime", "20:00").trim().split(":");
		ORC_FORTRESS_ENABLE = config.getBoolean("OrcFortressEnable", true);
		ORC_FORTRESS_HOUR = Integer.parseInt(orcFortressTime[0]);
		ORC_FORTRESS_MINUTE = Integer.parseInt(orcFortressTime[1]);
	}
}
