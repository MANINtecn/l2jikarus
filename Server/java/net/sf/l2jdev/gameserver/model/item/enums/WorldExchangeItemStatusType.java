package net.sf.l2jdev.gameserver.model.item.enums;

public enum WorldExchangeItemStatusType
{
	WORLD_EXCHANGE_REGISTERED(0),
	WORLD_EXCHANGE_SOLD(1),
	WORLD_EXCHANGE_OUT_TIME(2),
	WORLD_EXCHANGE_NONE(3);

	private final int _id;

	private WorldExchangeItemStatusType(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}

	public static WorldExchangeItemStatusType getWorldExchangeItemStatusType(int id)
	{
		for (WorldExchangeItemStatusType type : values())
		{
			if (type.getId() == id)
			{
				return type;
			}
		}

		return null;
	}
}
