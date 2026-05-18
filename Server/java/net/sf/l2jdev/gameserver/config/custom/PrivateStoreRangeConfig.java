package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class PrivateStoreRangeConfig
{
	public static final String PRIVATE_STORE_RANGE_CONFIG_FILE = "./config/Custom/PrivateStoreRange.ini";
	public static int SHOP_MIN_RANGE_FROM_PLAYER;
	public static int SHOP_MIN_RANGE_FROM_NPC;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/PrivateStoreRange.ini");
		SHOP_MIN_RANGE_FROM_PLAYER = config.getInt("ShopMinRangeFromPlayer", 50);
		SHOP_MIN_RANGE_FROM_NPC = config.getInt("ShopMinRangeFromNpc", 100);
	}
}
