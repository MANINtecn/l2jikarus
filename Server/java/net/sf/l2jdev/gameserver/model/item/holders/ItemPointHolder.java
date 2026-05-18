package net.sf.l2jdev.gameserver.model.item.holders;

import net.sf.l2jdev.gameserver.model.StatSet;

public class ItemPointHolder extends ItemHolder
{
	private final int _points;

	public ItemPointHolder(StatSet params)
	{
		this(params.getInt("id"), params.getLong("count"), params.getInt("points"));
	}

	public ItemPointHolder(int id, long count, int points)
	{
		super(id, count);
		this._points = points;
	}

	public int getPoints()
	{
		return this._points;
	}

	@Override
	public String toString()
	{
		return "[" + this.getClass().getSimpleName() + "] ID: " + this.getId() + ", count: " + this.getCount() + ", points: " + this._points;
	}
}
