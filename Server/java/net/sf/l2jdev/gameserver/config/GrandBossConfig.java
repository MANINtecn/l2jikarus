package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class GrandBossConfig
{
	public static final String GRANDBOSS_CONFIG_FILE = "./config/GrandBoss.ini";
	public static int ANTHARAS_WAIT_TIME;
	public static int ANTHARAS_SPAWN_INTERVAL;
	public static int ANTHARAS_SPAWN_RANDOM;
	public static int BAIUM_SPAWN_INTERVAL;
	public static int BAIUM_SPAWN_RANDOM;
	public static boolean BAIUM_RECOGNIZE_HERO;
	public static int CORE_SPAWN_INTERVAL;
	public static int CORE_SPAWN_RANDOM;
	public static int ORFEN_SPAWN_INTERVAL;
	public static int ORFEN_SPAWN_RANDOM;
	public static int QUEEN_ANT_SPAWN_INTERVAL;
	public static int QUEEN_ANT_SPAWN_RANDOM;
	public static int ZAKEN_SPAWN_INTERVAL;
	public static int ZAKEN_SPAWN_RANDOM;
	public static int BALOK_HOUR;
	public static int BALOK_MINUTE;
	public static int BALOK_POINTS_PER_MONSTER;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/GrandBoss.ini");
		ANTHARAS_WAIT_TIME = config.getInt("AntharasWaitTime", 30);
		ANTHARAS_SPAWN_INTERVAL = config.getInt("IntervalOfAntharasSpawn", 264);
		ANTHARAS_SPAWN_RANDOM = config.getInt("RandomOfAntharasSpawn", 72);
		BAIUM_SPAWN_INTERVAL = config.getInt("IntervalOfBaiumSpawn", 168);
		BAIUM_SPAWN_RANDOM = config.getInt("RandomOfBaiumSpawn", 48);
		BAIUM_RECOGNIZE_HERO = config.getBoolean("BaiumRecognizeHero", true);
		CORE_SPAWN_INTERVAL = config.getInt("IntervalOfCoreSpawn", 60);
		CORE_SPAWN_RANDOM = config.getInt("RandomOfCoreSpawn", 24);
		ORFEN_SPAWN_INTERVAL = config.getInt("IntervalOfOrfenSpawn", 48);
		ORFEN_SPAWN_RANDOM = config.getInt("RandomOfOrfenSpawn", 20);
		QUEEN_ANT_SPAWN_INTERVAL = config.getInt("IntervalOfQueenAntSpawn", 36);
		QUEEN_ANT_SPAWN_RANDOM = config.getInt("RandomOfQueenAntSpawn", 17);
		ZAKEN_SPAWN_INTERVAL = config.getInt("IntervalOfZakenSpawn", 168);
		ZAKEN_SPAWN_RANDOM = config.getInt("RandomOfZakenSpawn", 48);
		String[] balokTime = config.getString("BalokTime", "20:30").trim().split(":");
		BALOK_HOUR = Integer.parseInt(balokTime[0]);
		BALOK_MINUTE = Integer.parseInt(balokTime[1]);
		BALOK_POINTS_PER_MONSTER = config.getInt("BalokPointsPerMonster", 10);
	}
}
