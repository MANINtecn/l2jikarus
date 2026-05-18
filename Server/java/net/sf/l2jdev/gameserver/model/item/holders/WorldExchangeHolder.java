package net.sf.l2jdev.gameserver.model.item.holders;

import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.item.enums.WorldExchangeItemStatusType;
import net.sf.l2jdev.gameserver.model.item.enums.WorldExchangeItemSubType;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class WorldExchangeHolder
{
	private final long _worldExchangeId;
	private final Item _itemInstance;
	private final ItemInfo _itemInfo;
	private final long _price;
	private final int _oldOwnerId;
	private WorldExchangeItemStatusType _storeType;
	private final WorldExchangeItemSubType _category;
	private final long _startTime;
	private long _endTime;
	private boolean _hasChanges;

	public WorldExchangeHolder(long worldExchangeId, Item itemInstance, ItemInfo itemInfo, long price, int oldOwnerId, WorldExchangeItemStatusType storeType, WorldExchangeItemSubType category, long startTime, long endTime, boolean hasChanges)
	{
		this._worldExchangeId = worldExchangeId;
		this._itemInstance = itemInstance;
		this._itemInfo = itemInfo;
		this._price = price;
		this._oldOwnerId = oldOwnerId;
		this._storeType = storeType;
		this._category = category;
		this._startTime = startTime;
		this._endTime = endTime;
		this._hasChanges = hasChanges;
	}

	public long getWorldExchangeId()
	{
		return this._worldExchangeId;
	}

	public Item getItemInstance()
	{
		return this._itemInstance;
	}

	public ItemInfo getItemInfo()
	{
		return this._itemInfo;
	}

	public long getPrice()
	{
		return this._price;
	}

	public int getOldOwnerId()
	{
		return this._oldOwnerId;
	}

	public WorldExchangeItemStatusType getStoreType()
	{
		return this._storeType;
	}

	public void setStoreType(WorldExchangeItemStatusType storeType)
	{
		this._storeType = storeType;
	}

	public WorldExchangeItemSubType getCategory()
	{
		return this._category;
	}

	public long getStartTime()
	{
		return this._startTime;
	}

	public long getEndTime()
	{
		return this._endTime;
	}

	public void setEndTime(long endTime)
	{
		this._endTime = endTime;
	}

	public boolean hasChanges()
	{
		if (this._hasChanges)
		{
			this._hasChanges = false;
			return true;
		}
		return false;
	}

	public void setHasChanges(boolean hasChanges)
	{
		this._hasChanges = hasChanges;
	}
}
