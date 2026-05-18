package net.sf.l2jdev.gameserver.data.holders;

import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;

public class CrystallizationDataHolder
{
	private final int _id;
	private final List<ItemChanceHolder> _items;

	public CrystallizationDataHolder(int id, List<ItemChanceHolder> items)
	{
		this._id = id;
		this._items = Collections.unmodifiableList(items);
	}

	public int getId()
	{
		return this._id;
	}

	public List<ItemChanceHolder> getItems()
	{
		return this._items;
	}
}
