package net.sf.l2jdev.gameserver.data.holders;

import net.sf.l2jdev.gameserver.data.enums.LampType;

public class MagicLampHolder
{
	private final MagicLampDataHolder _lamp;
	private int _count;
	private long _exp;
	private long _sp;

	public MagicLampHolder(MagicLampDataHolder lamp)
	{
		this._lamp = lamp;
	}

	public void inc()
	{
		this._count++;
		this._exp = this._exp + this._lamp.getExp();
		this._sp = this._sp + this._lamp.getSp();
	}

	public LampType getType()
	{
		return this._lamp.getType();
	}

	public int getCount()
	{
		return this._count;
	}

	public long getExp()
	{
		return this._exp;
	}

	public long getSp()
	{
		return this._sp;
	}
}
