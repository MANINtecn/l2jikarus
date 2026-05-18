package net.sf.l2jdev.gameserver.model.item.enums;

public enum WorldExchangeItemMainType
{
	ADENA(24),
	EQUIPMENT(0),
	ENCHANT(8),
	CONSUMABLE(20),
	COLLECTION(5);

	private final int _id;

	private WorldExchangeItemMainType(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}

	public static WorldExchangeItemMainType getWorldExchangeItemMainType(int id)
	{
		for (WorldExchangeItemMainType type : values())
		{
			if (type.getId() == id)
			{
				return type;
			}
		}

		return null;
	}
}
