package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class AchievementBoxConfig
{
	public static final String ACHIEVEMENT_BOX_CONFIG_FILE = "./config/AchievementBox.ini";
	public static boolean ENABLE_ACHIEVEMENT_BOX;
	public static int ACHIEVEMENT_BOX_POINTS_FOR_REWARD;
	public static boolean ENABLE_ACHIEVEMENT_PVP;
	public static int ACHIEVEMENT_BOX_PVP_POINTS_FOR_REWARD;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/AchievementBox.ini");
		ENABLE_ACHIEVEMENT_BOX = config.getBoolean("EnabledAchievementBox", true);
		ACHIEVEMENT_BOX_POINTS_FOR_REWARD = config.getInt("PointsForReward", 1000);
		ENABLE_ACHIEVEMENT_PVP = config.getBoolean("EnabledAchievementPvP", true);
		ACHIEVEMENT_BOX_PVP_POINTS_FOR_REWARD = config.getInt("PointsForPvpReward", 5);
	}
}
