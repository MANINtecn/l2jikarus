package net.sf.l2jdev.gameserver.model.announce;

import java.util.logging.Level;
import java.util.logging.Logger;

public enum AnnouncementType
{
	NORMAL,
	CRITICAL,
	EVENT,
	AUTO_NORMAL,
	AUTO_CRITICAL;

	private static final Logger LOGGER = Logger.getLogger(AnnouncementType.class.getName());

	public static AnnouncementType findById(int id)
	{
		for (AnnouncementType type : values())
		{
			if (type.ordinal() == id)
			{
				return type;
			}
		}

		LOGGER.log(Level.WARNING, AnnouncementType.class.getSimpleName() + ": Unexistent id specified: " + id + "!", (new IllegalStateException()));
		return NORMAL;
	}

	public static AnnouncementType findByName(String name)
	{
		for (AnnouncementType type : values())
		{
			if (type.name().equalsIgnoreCase(name))
			{
				return type;
			}
		}

		LOGGER.log(Level.WARNING, AnnouncementType.class.getSimpleName() + ": Unexistent name specified: " + name + "!", (new IllegalStateException()));
		return NORMAL;
	}
}
