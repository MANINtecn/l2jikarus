package net.sf.l2jdev.gameserver.model;

public class FortSiegeSpawn extends Location
{
	private final int _npcId;
	private final int _fortId;
	private final int _id;

	public FortSiegeSpawn(int fortId, int x, int y, int z, int heading, int npcId, int id)
	{
		super(x, y, z, heading);
		this._fortId = fortId;
		this._npcId = npcId;
		this._id = id;
	}

	public int getFortId()
	{
		return this._fortId;
	}

	public int getId()
	{
		return this._npcId;
	}

	public int getMessageId()
	{
		return this._id;
	}
}
