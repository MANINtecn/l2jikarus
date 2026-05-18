package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.model.StatSet;

public class GreaterMagicLampHolder
{
	private final int _itemId;
	private final long _count;

	public GreaterMagicLampHolder(StatSet params)
	{
		this._itemId = params.getInt("item");
		this._count = params.getLong("count");
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public long getCount()
	{
		return this._count;
	}
}
