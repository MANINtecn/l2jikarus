package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class AllowedPlayerRacesConfig
{
	public static final String ALLOWED_PLAYER_RACES_CONFIG_FILE = "./config/Custom/AllowedPlayerRaces.ini";
	public static boolean ALLOW_HUMAN;
	public static boolean ALLOW_ELF;
	public static boolean ALLOW_DARKELF;
	public static boolean ALLOW_ORC;
	public static boolean ALLOW_DWARF;
	public static boolean ALLOW_KAMAEL;
	public static boolean ALLOW_DEATH_KNIGHT;
	public static boolean ALLOW_SYLPH;
	public static boolean ALLOW_VANGUARD;
	public static boolean ALLOW_ASSASSIN;
	public static boolean ALLOW_HIGH_ELF;
	public static boolean ALLOW_WARG;
	public static boolean ALLOW_BLOOD_ROSE;
	public static boolean ALLOW_SAMURAI;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/AllowedPlayerRaces.ini");
		ALLOW_HUMAN = config.getBoolean("AllowHuman", true);
		ALLOW_ELF = config.getBoolean("AllowElf", true);
		ALLOW_DARKELF = config.getBoolean("AllowDarkElf", true);
		ALLOW_ORC = config.getBoolean("AllowOrc", true);
		ALLOW_DWARF = config.getBoolean("AllowDwarf", true);
		ALLOW_KAMAEL = config.getBoolean("AllowKamael", true);
		ALLOW_DEATH_KNIGHT = config.getBoolean("AllowDeathKnight", true);
		ALLOW_SYLPH = config.getBoolean("AllowSylph", true);
		ALLOW_VANGUARD = config.getBoolean("AllowVanguard", true);
		ALLOW_ASSASSIN = config.getBoolean("AllowAssassin", true);
		ALLOW_HIGH_ELF = config.getBoolean("AllowHighElf", true);
		ALLOW_WARG = config.getBoolean("AllowWarg", true);
		ALLOW_BLOOD_ROSE = config.getBoolean("AllowBloodRose", true);
		ALLOW_SAMURAI = config.getBoolean("AllowSamurai", true);
	}
}
