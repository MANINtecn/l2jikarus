package net.sf.l2jdev.gameserver.model.item.holders;

import net.sf.l2jdev.gameserver.model.StatSet;

public class ItemHolder
{
	private final int _id;
	private final long _count;

	public ItemHolder(StatSet set)
	{
		this._id = set.getInt("id");
		this._count = set.getLong("count");
	}

	public ItemHolder(int id, long count)
	{
		this._id = id;
		this._count = count;
	}

	public int getId()
	{
		return this._id;
	}

	public long getCount()
	{
		return this._count;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (!(obj instanceof ItemHolder))
		{
			return false;
		}
		else if (obj == this)
		{
			return true;
		}
		else
		{
			ItemHolder objInstance = (ItemHolder) obj;
			return this._id == objInstance.getId() && this._count == objInstance.getCount();
		}
	}

	@Override
	public String toString()
	{
		return "[" + this.getClass().getSimpleName() + "] ID: " + this._id + ", count: " + this._count;
	}
}
