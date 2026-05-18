package net.sf.l2jdev.gameserver.model.actor.enums.player;

public enum IllegalActionPunishmentType
{
	NONE,
	BROADCAST,
	KICK,
	KICKBAN,
	JAIL;

	public static IllegalActionPunishmentType findByName(String name)
	{
		for (IllegalActionPunishmentType type : values())
		{
			if (type.name().equalsIgnoreCase(name))
			{
				return type;
			}
		}

		return NONE;
	}
}
