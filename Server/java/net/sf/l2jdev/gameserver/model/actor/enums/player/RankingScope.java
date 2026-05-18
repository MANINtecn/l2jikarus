package net.sf.l2jdev.gameserver.model.actor.enums.player;

public enum RankingScope
{
	TOP_100(0),
	TOP_150(0),
	ALL(0),
	SELF(1);

	private final int _id;

	private RankingScope(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}
}
