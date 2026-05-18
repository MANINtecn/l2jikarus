package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class RelicSystemConfig
{
	public static final String RELIC_SYSTEM_CONFIG_FILE = "./config/RelicSystem.ini";
	public static boolean RELIC_SYSTEM_ENABLED;
	public static boolean RELIC_SYSTEM_DEBUG_ENABLED;
	public static boolean RELIC_SUMMON_ANNOUNCE;
	public static boolean RELIC_ANNOUNCE_ONLY_A_B_GRADE;
	public static int RELIC_UNCONFIRMED_LIST_LIMIT;
	public static int RELIC_UNCONFIRMED_TIME_LIMIT;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/RelicSystem.ini");
		RELIC_SYSTEM_ENABLED = config.getBoolean("RelicSystemEnabled", true);
		RELIC_SYSTEM_DEBUG_ENABLED = config.getBoolean("RelicSystemDebugEnabled", false);
		RELIC_SUMMON_ANNOUNCE = config.getBoolean("RelicSummonAnnounce", true);
		RELIC_ANNOUNCE_ONLY_A_B_GRADE = config.getBoolean("RelicAnnounceOnlyABGrade", true);
		RELIC_UNCONFIRMED_LIST_LIMIT = config.getInt("RelicUnconfirmedListLimit", 100);
		RELIC_UNCONFIRMED_TIME_LIMIT = config.getInt("RelicUnconfirmedTimeLimit", 7);
	}
}
