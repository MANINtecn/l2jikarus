package net.sf.l2jdev.gameserver.data.holders;

import java.util.List;

import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;

public class CollectionDataHolder
{
	private final int _collectionId;
	private final int _optionId;
	private final int _category;
	private final int _completeCount;
	private final List<ItemEnchantHolder> _items;

	public CollectionDataHolder(int collectionId, int optionId, int category, int completeCount, List<ItemEnchantHolder> items)
	{
		this._collectionId = collectionId;
		this._optionId = optionId;
		this._category = category;
		this._completeCount = completeCount;
		this._items = items;
	}

	public int getCollectionId()
	{
		return this._collectionId;
	}

	public int getOptionId()
	{
		return this._optionId;
	}

	public int getCategory()
	{
		return this._category;
	}

	public int getCompleteCount()
	{
		return this._completeCount;
	}

	public List<ItemEnchantHolder> getItems()
	{
		return this._items;
	}
}
