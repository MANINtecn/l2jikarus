package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;
import net.sf.l2jdev.gameserver.model.Location;

public class FactionSystemConfig
{
	public static final String FACTION_SYSTEM_CONFIG_FILE = "./config/Custom/FactionSystem.ini";
	public static boolean FACTION_SYSTEM_ENABLED;
	public static Location FACTION_STARTING_LOCATION;
	public static Location FACTION_MANAGER_LOCATION;
	public static Location FACTION_GOOD_BASE_LOCATION;
	public static Location FACTION_EVIL_BASE_LOCATION;
	public static String FACTION_GOOD_TEAM_NAME;
	public static String FACTION_EVIL_TEAM_NAME;
	public static int FACTION_GOOD_NAME_COLOR;
	public static int FACTION_EVIL_NAME_COLOR;
	public static boolean FACTION_GUARDS_ENABLED;
	public static boolean FACTION_RESPAWN_AT_BASE;
	public static boolean FACTION_AUTO_NOBLESS;
	public static boolean FACTION_SPECIFIC_CHAT;
	public static boolean FACTION_BALANCE_ONLINE_PLAYERS;
	public static int FACTION_BALANCE_PLAYER_EXCEED_LIMIT;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/FactionSystem.ini");
		FACTION_SYSTEM_ENABLED = config.getBoolean("EnableFactionSystem", false);
		String[] tempString = config.getString("StartingLocation", "85332,16199,-1252").split(",");
		FACTION_STARTING_LOCATION = new Location(Integer.parseInt(tempString[0]), Integer.parseInt(tempString[1]), Integer.parseInt(tempString[2]));
		tempString = config.getString("ManagerSpawnLocation", "85712,15974,-1260,26808").split(",");
		FACTION_MANAGER_LOCATION = new Location(Integer.parseInt(tempString[0]), Integer.parseInt(tempString[1]), Integer.parseInt(tempString[2]), tempString[3] != null ? Integer.parseInt(tempString[3]) : 0);
		tempString = config.getString("GoodBaseLocation", "45306,48878,-3058").split(",");
		FACTION_GOOD_BASE_LOCATION = new Location(Integer.parseInt(tempString[0]), Integer.parseInt(tempString[1]), Integer.parseInt(tempString[2]));
		tempString = config.getString("EvilBaseLocation", "-44037,-113283,-237").split(",");
		FACTION_EVIL_BASE_LOCATION = new Location(Integer.parseInt(tempString[0]), Integer.parseInt(tempString[1]), Integer.parseInt(tempString[2]));
		FACTION_GOOD_TEAM_NAME = config.getString("GoodTeamName", "Good");
		FACTION_EVIL_TEAM_NAME = config.getString("EvilTeamName", "Evil");
		FACTION_GOOD_NAME_COLOR = Integer.decode("0x" + config.getString("GoodNameColor", "00FF00"));
		FACTION_EVIL_NAME_COLOR = Integer.decode("0x" + config.getString("EvilNameColor", "0000FF"));
		FACTION_GUARDS_ENABLED = config.getBoolean("EnableFactionGuards", true);
		FACTION_RESPAWN_AT_BASE = config.getBoolean("RespawnAtFactionBase", true);
		FACTION_AUTO_NOBLESS = config.getBoolean("FactionAutoNobless", false);
		FACTION_SPECIFIC_CHAT = config.getBoolean("EnableFactionChat", true);
		FACTION_BALANCE_ONLINE_PLAYERS = config.getBoolean("BalanceOnlinePlayers", true);
		FACTION_BALANCE_PLAYER_EXCEED_LIMIT = config.getInt("BalancePlayerExceedLimit", 20);
	}
}
