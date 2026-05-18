package net.sf.l2jdev.gameserver.model.siege;

public class MercenaryPledgeHolder
{
	private final int _playerId;
	private final String _name;
	private final int _classId;
	private final int _clanId;

	public MercenaryPledgeHolder(int playerId, String name, int classId, int clanId)
	{
		this._playerId = playerId;
		this._name = name;
		this._classId = classId;
		this._clanId = clanId;
	}

	public int getPlayerId()
	{
		return this._playerId;
	}

	public String getName()
	{
		return this._name;
	}

	public int getClassId()
	{
		return this._classId;
	}

	public int getClanId()
	{
		return this._clanId;
	}
}
