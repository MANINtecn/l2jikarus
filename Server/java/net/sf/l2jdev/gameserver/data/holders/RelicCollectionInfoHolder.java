package net.sf.l2jdev.gameserver.data.holders;

public class RelicCollectionInfoHolder
{
	private final int _id;
	private final int _enchant;

	public RelicCollectionInfoHolder(int id, int enchant)
	{
		this._id = id;
		this._enchant = enchant;
	}

	public int getRelicId()
	{
		return this._id;
	}

	public int getEnchantLevel()
	{
		return this._enchant;
	}
}
