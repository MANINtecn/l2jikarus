package net.sf.l2jdev.gameserver.model.item.holders;

import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class PetItemHolder
{
	private final Item _item;

	public PetItemHolder(Item item)
	{
		this._item = item;
	}

	public Item getItem()
	{
		return this._item;
	}
}
