package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class MerchantZeroSellPriceConfig
{
	public static final String MERCHANT_ZERO_SELL_PRICE_CONFIG_FILE = "./config/Custom/MerchantZeroSellPrice.ini";
	public static boolean MERCHANT_ZERO_SELL_PRICE;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/MerchantZeroSellPrice.ini");
		MERCHANT_ZERO_SELL_PRICE = config.getBoolean("MerchantZeroSellPrice", false);
	}
}
