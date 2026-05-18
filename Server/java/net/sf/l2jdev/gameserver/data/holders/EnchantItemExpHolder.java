package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.model.StatSet;

public class EnchantItemExpHolder
{
	private final int _id;
	private final int _exp;
	private final int _starLevel;

	public EnchantItemExpHolder(StatSet set)
	{
		this._id = set.getInt("id", 1);
		this._exp = set.getInt("exp", 1);
		this._starLevel = set.getInt("starLevel", 1);
	}

	public int getStarLevel()
	{
		return this._starLevel;
	}

	public int getId()
	{
		return this._id;
	}

	public int getExp()
	{
		return this._exp;
	}
}
