package net.sf.l2jdev.gameserver.model;

import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class TempItem
{
	private final int _itemId;
	private int _quantity;
	private final long _referencePrice;
	private final String _itemName;

	public TempItem(Item item, int quantity)
	{
		this._itemId = item.getId();
		this._quantity = quantity;
		this._itemName = item.getTemplate().getName();
		this._referencePrice = item.getReferencePrice();
	}

	public int getQuantity()
	{
		return this._quantity;
	}

	public void setQuantity(int quantity)
	{
		this._quantity = quantity;
	}

	public long getReferencePrice()
	{
		return this._referencePrice;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public String getItemName()
	{
		return this._itemName;
	}
}
