package net.sf.l2jdev.gameserver.model.item.holders;

public class ItemRestoreHolder
{
	private final int _ownerId;
	private final int _destroyedItemId;
	private int _repairItemId;
	private final short _enchantLevel;
	private final long _destroyDate;
	private boolean _needToStore;

	public ItemRestoreHolder(int ownerId, int destroyedItemId, short enchantLevel, long destroyDate, boolean needToStore)
	{
		this._ownerId = ownerId;
		this._destroyedItemId = destroyedItemId;
		this._repairItemId = destroyedItemId;
		this._enchantLevel = enchantLevel;
		this._destroyDate = destroyDate;
		this._needToStore = needToStore;
	}

	public int getOwnerId()
	{
		return this._ownerId;
	}

	public int getDestroyedItemId()
	{
		return this._destroyedItemId;
	}

	public void setRepairItemId(int itemId)
	{
		this._repairItemId = itemId;
	}

	public int getRepairItemId()
	{
		return this._repairItemId;
	}

	public short getEnchantLevel()
	{
		return this._enchantLevel;
	}

	public long getDestroyDate()
	{
		return this._destroyDate;
	}

	public boolean needToStore()
	{
		return this._needToStore;
	}

	public void setNeedToStore(boolean needToStore)
	{
		this._needToStore = needToStore;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (this == obj)
		{
			return true;
		}
		else if (obj != null && this.getClass() == obj.getClass())
		{
			ItemRestoreHolder other = (ItemRestoreHolder) obj;
			return this._ownerId == other._ownerId && this._destroyedItemId == other._destroyedItemId && this._repairItemId == other._repairItemId && this._enchantLevel == other._enchantLevel && this._destroyDate == other._destroyDate;
		}
		else
		{
			return false;
		}
	}
}
