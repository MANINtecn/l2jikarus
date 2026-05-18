package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class VipSystemConfig
{
	public static final String CUSTOM_VIP_CONFIG_FILE = "./config/Custom/VipSystem.ini";
	public static boolean VIP_SYSTEM_ENABLED;
	public static boolean VIP_SYSTEM_PRIME_AFFECT;
	public static boolean VIP_SYSTEM_L_SHOP_AFFECT;
	public static int VIP_SYSTEM_MAX_TIER;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/VipSystem.ini");
		VIP_SYSTEM_ENABLED = config.getBoolean("VipEnabled", false);
		if (VIP_SYSTEM_ENABLED)
		{
			VIP_SYSTEM_PRIME_AFFECT = config.getBoolean("PrimeAffectPoints", false);
			VIP_SYSTEM_L_SHOP_AFFECT = config.getBoolean("LShopAffectPoints", false);
			VIP_SYSTEM_MAX_TIER = config.getInt("MaxVipLevel", 7);
			if (VIP_SYSTEM_MAX_TIER > 10)
			{
				VIP_SYSTEM_MAX_TIER = 10;
			}
		}
	}
}
