package net.sf.l2jdev.gameserver.model.actor.holders.player;

public class CrossEventAdvancedRewardHolder
{
	private final int _itemId;
	private final long _itemCount;
	private final int _itemChance;

	public CrossEventAdvancedRewardHolder(int itemId, int itemCount, double itemChance)
	{
		this._itemId = itemId;
		this._itemCount = itemCount;
		this._itemChance = (int) (itemChance * 1000.0);
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public long getCount()
	{
		return this._itemCount;
	}

	public int getChance()
	{
		return this._itemChance;
	}
}
