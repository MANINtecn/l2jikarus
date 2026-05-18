package net.sf.l2jdev.gameserver.model.fishing;

public class FishingRod
{
	private final int _itemId;
	private final int _reduceFishingTime;
	private final float _xpMultiplier;
	private final float _spMultiplier;

	public FishingRod(int itemId, int reduceFishingTime, float xpMultiplier, float spMultiplier)
	{
		this._itemId = itemId;
		this._reduceFishingTime = reduceFishingTime;
		this._xpMultiplier = xpMultiplier;
		this._spMultiplier = spMultiplier;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public int getReduceFishingTime()
	{
		return this._reduceFishingTime;
	}

	public float getXpMultiplier()
	{
		return this._xpMultiplier;
	}

	public float getSpMultiplier()
	{
		return this._spMultiplier;
	}
}
