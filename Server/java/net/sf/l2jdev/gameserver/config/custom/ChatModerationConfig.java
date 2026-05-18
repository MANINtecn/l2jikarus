package net.sf.l2jdev.gameserver.config.custom;

import net.sf.l2jdev.commons.util.ConfigReader;

public class ChatModerationConfig
{
	public static final String CHAT_MODERATION_CONFIG_FILE = "./config/Custom/ChatModeration.ini";
	public static boolean CHAT_ADMIN;

	public static void load()
	{
		ConfigReader config = new ConfigReader("./config/Custom/ChatModeration.ini");
		CHAT_ADMIN = config.getBoolean("ChatAdmin", true);
	}
}
