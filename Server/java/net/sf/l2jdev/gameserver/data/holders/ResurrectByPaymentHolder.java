package net.sf.l2jdev.gameserver.data.holders;

public class ResurrectByPaymentHolder
{
	private final int _time;
	private final int _amount;
	private final double _resurrectPercent;

	public ResurrectByPaymentHolder(int time, int amount, double percent)
	{
		this._time = time;
		this._amount = amount;
		this._resurrectPercent = percent;
	}

	public int getTime()
	{
		return this._time;
	}

	public int getAmount()
	{
		return this._amount;
	}

	public double getResurrectPercent()
	{
		return this._resurrectPercent;
	}
}
