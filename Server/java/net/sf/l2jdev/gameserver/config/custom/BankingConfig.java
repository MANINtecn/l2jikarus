package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class BankingConfig
{
	public static final String BANKING_CONFIG_FILE = "./config/Custom/Banking.ini";
	public static boolean BANKING_SYSTEM_ENABLED;
	public static int BANKING_SYSTEM_GOLDBARS;
	public static int BANKING_SYSTEM_ADENA;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/Banking.ini");
		BANKING_SYSTEM_ENABLED = config.getBoolean("BankingEnabled", false);
		BANKING_SYSTEM_GOLDBARS = config.getInt("BankingGoldbarCount", 1);
		BANKING_SYSTEM_ADENA = config.getInt("BankingAdenaCount", 500000000);
	}
}
