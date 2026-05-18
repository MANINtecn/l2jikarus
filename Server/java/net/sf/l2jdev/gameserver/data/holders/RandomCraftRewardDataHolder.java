package net.sf.l2jdev.gameserver.data.holders;

public class RandomCraftRewardDataHolder
{
	private final int _itemId;
	private final long _count;
	private final double _chance;
	private final boolean _announce;

	public RandomCraftRewardDataHolder(int itemId, long count, double chance, boolean announce)
	{
		this._itemId = itemId;
		this._count = count;
		this._chance = chance;
		this._announce = announce;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public long getCount()
	{
		return this._count;
	}

	public double getChance()
	{
		return this._chance;
	}

	public boolean isAnnounce()
	{
		return this._announce;
	}
}
