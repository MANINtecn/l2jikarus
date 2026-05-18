package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.data.enums.LampType;
import net.sf.l2jdev.gameserver.model.StatSet;

public class MagicLampDataHolder
{
	private final LampType _type;
	private final long _exp;
	private final long _sp;
	private final double _chance;
	private final int _fromLevel;
	private final int _toLevel;

	public MagicLampDataHolder(StatSet params)
	{
		this._type = params.getEnum("type", LampType.class);
		this._exp = params.getLong("exp");
		this._sp = params.getLong("sp");
		this._chance = params.getDouble("chance");
		this._fromLevel = params.getInt("minLevel");
		this._toLevel = params.getInt("maxLevel");
	}

	public LampType getType()
	{
		return this._type;
	}

	public long getExp()
	{
		return this._exp;
	}

	public long getSp()
	{
		return this._sp;
	}

	public double getChance()
	{
		return this._chance;
	}

	public int getFromLevel()
	{
		return this._fromLevel;
	}

	public int getToLevel()
	{
		return this._toLevel;
	}
}
