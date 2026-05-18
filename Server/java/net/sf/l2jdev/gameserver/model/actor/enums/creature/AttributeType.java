package net.sf.l2jdev.gameserver.model.actor.enums.creature;

public enum AttributeType
{
	NONE(-2),
	FIRE(0),
	WATER(1),
	WIND(2),
	EARTH(3),
	HOLY(4),
	DARK(5);

	public static final AttributeType[] ATTRIBUTE_TYPES = new AttributeType[]
	{
		FIRE,
		WATER,
		WIND,
		EARTH,
		HOLY,
		DARK
	};
	private final byte _clientId;

	private AttributeType(int clientId)
	{
		this._clientId = (byte) clientId;
	}

	public byte getClientId()
	{
		return this._clientId;
	}

	public AttributeType getOpposite()
	{
		return ATTRIBUTE_TYPES[this._clientId % 2 == 0 ? this._clientId + 1 : this._clientId - 1];
	}

	public static AttributeType findByName(String attributeName)
	{
		for (AttributeType attributeType : values())
		{
			if (attributeType.name().equalsIgnoreCase(attributeName))
			{
				return attributeType;
			}
		}

		return null;
	}

	public static AttributeType findByClientId(int clientId)
	{
		for (AttributeType attributeType : values())
		{
			if (attributeType.getClientId() == clientId)
			{
				return attributeType;
			}
		}

		return null;
	}
}
