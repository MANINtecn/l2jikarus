package net.sf.l2jdev.gameserver.model.primeshop;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class PrimeShopItem extends ItemHolder
{
	private final int _weight;
	private final int _isTradable;

	public PrimeShopItem(int itemId, int count, int weight, int isTradable)
	{
		super(itemId, count);
		this._weight = weight;
		this._isTradable = isTradable;
	}

	public int getWeight()
	{
		return this._weight;
	}

	public int isTradable()
	{
		return this._isTradable;
	}
}
