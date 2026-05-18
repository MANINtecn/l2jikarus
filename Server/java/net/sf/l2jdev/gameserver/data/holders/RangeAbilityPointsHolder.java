package net.sf.l2jdev.gameserver.data.holders;

public class RangeAbilityPointsHolder
{
	private final int _min;
	private final int _max;
	private final long _sp;

	public RangeAbilityPointsHolder(int min, int max, long sp)
	{
		this._min = min;
		this._max = max;
		this._sp = sp;
	}

	public int getMin()
	{
		return this._min;
	}

	public int getMax()
	{
		return this._max;
	}

	public long getSP()
	{
		return this._sp;
	}
}
