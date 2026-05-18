package net.sf.l2jdev.gameserver.config;

import net.sf.l2jdev.commons.util.ConfigReader;

public class WorldExchangeConfig
{
	public static final String WORLD_EXCHANGE_FILE = "./config/WorldExchange.ini";
	public static boolean ENABLE_WORLD_EXCHANGE;
	public static String WORLD_EXCHANGE_DEFAULT_LANG;
	public static long WORLD_EXCHANGE_SAVE_INTERVAL;
	public static double WORLD_EXCHANGE_LCOIN_TAX;
	public static long WORLD_EXCHANGE_MAX_LCOIN_TAX;
	public static double WORLD_EXCHANGE_ADENA_FEE;
	public static long WORLD_EXCHANGE_MAX_ADENA_FEE;
	public static boolean WORLD_EXCHANGE_LAZY_UPDATE;
	public static int WORLD_EXCHANGE_ITEM_SELL_PERIOD;
	public static int WORLD_EXCHANGE_ITEM_BACK_PERIOD;
	public static int WORLD_EXCHANGE_PAYMENT_TAKE_PERIOD;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/WorldExchange.ini");
		ENABLE_WORLD_EXCHANGE = config.getBoolean("EnableWorldExchange", true);
		WORLD_EXCHANGE_DEFAULT_LANG = config.getString("WorldExchangeDefaultLanguage", "en");
		WORLD_EXCHANGE_SAVE_INTERVAL = config.getLong("BidItemsIntervalStatusCheck", 30000L);
		WORLD_EXCHANGE_LCOIN_TAX = config.getDouble("LCoinFee", 0.05);
		WORLD_EXCHANGE_MAX_LCOIN_TAX = config.getLong("MaxLCoinFee", 20000L);
		WORLD_EXCHANGE_ADENA_FEE = config.getDouble("AdenaFee", 10000.0);
		WORLD_EXCHANGE_MAX_ADENA_FEE = config.getLong("MaxAdenaFee", -1L);
		WORLD_EXCHANGE_LAZY_UPDATE = config.getBoolean("DBLazy", false);
		WORLD_EXCHANGE_ITEM_SELL_PERIOD = config.getInt("ItemSellPeriod", 14);
		WORLD_EXCHANGE_ITEM_BACK_PERIOD = config.getInt("ItemBackPeriod", 120);
		WORLD_EXCHANGE_PAYMENT_TAKE_PERIOD = config.getInt("PaymentTakePeriod", 120);
	}
}
