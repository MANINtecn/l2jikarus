package net.sf.l2jdev.gameserver.model.groups.matching;

public class MatchingRoomHistory
{
	private final String _title;
	private final String _leader;

	public MatchingRoomHistory(String title, String leader)
	{
		this._title = title;
		this._leader = leader;
	}

	public String getTitle()
	{
		return this._title;
	}

	public String getLeader()
	{
		return this._leader;
	}
}
