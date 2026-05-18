package net.sf.l2jdev.commons.config;

import java.awt.GraphicsEnvironment;

import net.sf.l2jdev.commons.util.ConfigReader;

public class InterfaceConfig
{
	public static final String INTERFACE_CONFIG_FILE = "./config/Interface.ini";
	public static boolean ENABLE_GUI;
	public static boolean DARK_THEME;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Interface.ini");
		ENABLE_GUI = config.getBoolean("EnableGUI", true) && !GraphicsEnvironment.isHeadless();
		if (ENABLE_GUI)
		{
			DARK_THEME = config.getBoolean("DarkTheme", true);
		}
	}
}
