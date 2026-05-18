package net.sf.l2jdev.gameserver.managers.events;

import java.util.HashMap;
import java.util.Map;
import java.util.logging.Logger;

public class ItemDeletionInfoManager
{
	protected static final Logger LOGGER = Logger.getLogger(ItemDeletionInfoManager.class.getName());
	private final Map<Integer, Integer> _itemDates = new HashMap<>();

	protected ItemDeletionInfoManager()
	{
	}

	public void addItemDate(int itemId, int date)
	{
		this._itemDates.put(itemId, date);
	}

	public Map<Integer, Integer> getItemDates()
	{
		return this._itemDates;
	}

	public static ItemDeletionInfoManager getInstance()
	{
		return ItemDeletionInfoManager.SingletonHolder.INSTANCE;
	}

	private static class SingletonHolder
	{
		protected static final ItemDeletionInfoManager INSTANCE = new ItemDeletionInfoManager();
	}
}
