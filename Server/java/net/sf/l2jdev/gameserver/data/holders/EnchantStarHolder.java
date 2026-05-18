package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.model.StatSet;

public class EnchantStarHolder
{
	private final int _level;
	private final int _expMax;
	private final int _expOnFail;
	private final long _feeAdena;

	public EnchantStarHolder(StatSet set)
	{
		this._level = set.getInt("level");
		this._expMax = set.getInt("expMax");
		this._expOnFail = set.getInt("expOnFail");
		this._feeAdena = set.getLong("feeAdena");
	}

	public int getLevel()
	{
		return this._level;
	}

	public int getExpMax()
	{
		return this._expMax;
	}

	public int getExpOnFail()
	{
		return this._expOnFail;
	}

	public long getFeeAdena()
	{
		return this._feeAdena;
	}
}
