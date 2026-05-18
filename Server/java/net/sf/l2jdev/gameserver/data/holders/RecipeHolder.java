package net.sf.l2jdev.gameserver.data.holders;

public class RecipeHolder
{
	private final int _itemId;
	private final int _quantity;

	public RecipeHolder(int itemId, int quantity)
	{
		this._itemId = itemId;
		this._quantity = quantity;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public int getQuantity()
	{
		return this._quantity;
	}
}
