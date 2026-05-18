package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class HuntPassConfig
{
	public static final String HUNT_PASS_CONFIG_FILE = "./config/HuntPass.ini";
	public static boolean ENABLE_HUNT_PASS;
	public static int HUNT_PASS_PREMIUM_ITEM_ID;
	public static int HUNT_PASS_PREMIUM_ITEM_COUNT;
	public static int HUNT_PASS_POINTS_FOR_STEP;
	public static int HUNT_PASS_PERIOD;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/HuntPass.ini");
		ENABLE_HUNT_PASS = config.getBoolean("EnabledHuntPass", true);
		HUNT_PASS_PREMIUM_ITEM_ID = config.getInt("PremiumItemId", 91663);
		HUNT_PASS_PREMIUM_ITEM_COUNT = config.getInt("PremiumItemCount", 3600);
		HUNT_PASS_POINTS_FOR_STEP = config.getInt("PointsForStep", 2400);
		HUNT_PASS_PERIOD = config.getInt("DayOfMonth", 1);
	}
}
