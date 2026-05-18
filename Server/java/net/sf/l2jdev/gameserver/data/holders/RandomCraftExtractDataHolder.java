package net.sf.l2jdev.gameserver.data.holders;

public class RandomCraftExtractDataHolder
{
	private final long _points;
	private final long _fee;

	public RandomCraftExtractDataHolder(long points, long fee)
	{
		this._points = points;
		this._fee = fee;
	}

	public long getPoints()
	{
		return this._points;
	}

	public long getFee()
	{
		return this._fee;
	}
}
