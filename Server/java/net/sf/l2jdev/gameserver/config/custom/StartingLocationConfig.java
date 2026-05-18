package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class StartingLocationConfig
{
	public static final String STARTING_LOCATION_CONFIG_FILE = "./config/Custom/StartingLocation.ini";
	public static boolean CUSTOM_STARTING_LOC;
	public static int CUSTOM_STARTING_LOC_X;
	public static int CUSTOM_STARTING_LOC_Y;
	public static int CUSTOM_STARTING_LOC_Z;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/StartingLocation.ini");
		CUSTOM_STARTING_LOC = config.getBoolean("CustomStartingLocation", false);
		CUSTOM_STARTING_LOC_X = config.getInt("CustomStartingLocX", 50821);
		CUSTOM_STARTING_LOC_Y = config.getInt("CustomStartingLocY", 186527);
		CUSTOM_STARTING_LOC_Z = config.getInt("CustomStartingLocZ", -3625);
	}
}
