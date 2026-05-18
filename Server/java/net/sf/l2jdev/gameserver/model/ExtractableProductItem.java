package net.sf.l2jdev.gameserver.model;

import java.util.List;

import net.sf.l2jdev.gameserver.model.item.holders.RestorationItemHolder;

public class ExtractableProductItem
{
	private final List<RestorationItemHolder> _items;
	private final double _chance;

	public ExtractableProductItem(List<RestorationItemHolder> items, double chance)
	{
		this._items = items;
		this._chance = chance;
	}

	public List<RestorationItemHolder> getItems()
	{
		return this._items;
	}

	public double getChance()
	{
		return this._chance;
	}
}
