package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class TrainingCampConfig
{
	public static final String TRAINING_CAMP_CONFIG_FILE = "./config/TrainingCamp.ini";
	public static boolean TRAINING_CAMP_ENABLE;
	public static boolean TRAINING_CAMP_PREMIUM_ONLY;
	public static int TRAINING_CAMP_MAX_DURATION;
	public static int TRAINING_CAMP_MIN_LEVEL;
	public static int TRAINING_CAMP_MAX_LEVEL;
	public static double TRAINING_CAMP_EXP_MULTIPLIER;
	public static double TRAINING_CAMP_SP_MULTIPLIER;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/TrainingCamp.ini");
		TRAINING_CAMP_ENABLE = config.getBoolean("TrainingCampEnable", false);
		TRAINING_CAMP_PREMIUM_ONLY = config.getBoolean("TrainingCampPremiumOnly", false);
		TRAINING_CAMP_MAX_DURATION = config.getInt("TrainingCampDuration", 18000);
		TRAINING_CAMP_MIN_LEVEL = config.getInt("TrainingCampMinLevel", 18);
		TRAINING_CAMP_MAX_LEVEL = config.getInt("TrainingCampMaxLevel", 127);
		TRAINING_CAMP_EXP_MULTIPLIER = config.getDouble("TrainingCampExpMultiplier", 1.0);
		TRAINING_CAMP_SP_MULTIPLIER = config.getDouble("TrainingCampSpMultiplier", 1.0);
	}
}
