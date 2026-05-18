package net.sf.l2jdev.gameserver.model.actor.holders.player;

public class RankingHistoryDataHolder
{
	private final int _rank;
	private final long _exp;
	private final long _day;

	public RankingHistoryDataHolder(long day, int rank, long exp)
	{
		this._day = day;
		this._rank = rank;
		this._exp = exp;
	}

	public long getDay()
	{
		return this._day;
	}

	public int getRank()
	{
		return this._rank;
	}

	public long getExp()
	{
		return this._exp;
	}
}
