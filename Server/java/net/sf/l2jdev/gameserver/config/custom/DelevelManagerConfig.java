package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class DelevelManagerConfig
{
	public static final String DELEVEL_MANAGER_CONFIG_FILE = "./config/Custom/DelevelManager.ini";
	public static boolean DELEVEL_MANAGER_ENABLED;
	public static int DELEVEL_MANAGER_NPCID;
	public static int DELEVEL_MANAGER_ITEMID;
	public static int DELEVEL_MANAGER_ITEMCOUNT;
	public static int DELEVEL_MANAGER_MINIMUM_DELEVEL;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/DelevelManager.ini");
		DELEVEL_MANAGER_ENABLED = config.getBoolean("Enabled", false);
		DELEVEL_MANAGER_NPCID = config.getInt("NpcId", 1002000);
		DELEVEL_MANAGER_ITEMID = config.getInt("RequiredItemId", 4356);
		DELEVEL_MANAGER_ITEMCOUNT = config.getInt("RequiredItemCount", 2);
		DELEVEL_MANAGER_MINIMUM_DELEVEL = config.getInt("MimimumDelevel", 20);
	}
}
