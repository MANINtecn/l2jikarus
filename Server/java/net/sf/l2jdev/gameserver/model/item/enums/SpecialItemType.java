package net.sf.l2jdev.gameserver.model.item.enums;

public enum SpecialItemType
{
	PC_CAFE_POINTS(-100),
	CLAN_REPUTATION(-200),
	FAME(-300),
	FIELD_CYCLE_POINTS(-400),
	RAIDBOSS_POINTS(-500),
	HONOR_COINS(-700);

	private final int _clientId;

	private SpecialItemType(int clientId)
	{
		this._clientId = clientId;
	}

	public int getClientId()
	{
		return this._clientId;
	}

	public static SpecialItemType getByClientId(int clientId)
	{
		for (SpecialItemType type : values())
		{
			if (type.getClientId() == clientId)
			{
				return type;
			}
		}

		return null;
	}
}
