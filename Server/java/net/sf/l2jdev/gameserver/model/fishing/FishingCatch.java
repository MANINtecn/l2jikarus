package net.sf.l2jdev.gameserver.model.fishing;

public class FishingCatch
{
	private final int _itemId;
	private final float _chance;
	private final float _multiplier;

	public FishingCatch(int itemId, float chance, float multiplier)
	{
		this._itemId = itemId;
		this._chance = chance;
		this._multiplier = multiplier;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public float getChance()
	{
		return this._chance;
	}

	public float getMultiplier()
	{
		return this._multiplier;
	}
}
