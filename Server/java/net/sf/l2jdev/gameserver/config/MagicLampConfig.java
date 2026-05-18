package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class MagicLampConfig
{
	public static final String MAGIC_LAMP_FILE = "./config/MagicLamp.ini";
	public static boolean ENABLE_MAGIC_LAMP;
	public static int MAGIC_LAMP_MAX_LEVEL_EXP;
	public static double MAGIC_LAMP_CHARGE_RATE;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/MagicLamp.ini");
		ENABLE_MAGIC_LAMP = config.getBoolean("MagicLampEnabled", false);
		MAGIC_LAMP_MAX_LEVEL_EXP = config.getInt("MagicLampMaxLevelExp", 10000000);
		MAGIC_LAMP_CHARGE_RATE = config.getDouble("MagicLampChargeRate", 0.1);
	}
}
