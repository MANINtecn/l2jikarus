package net.sf.l2jdev.gameserver.model.siege;

import net.sf.l2jdev.gameserver.model.Location;

public class CastleSpawnHolder extends Location
{
	private final int _npcId;
	private final CastleSide _side;

	public CastleSpawnHolder(int npcId, CastleSide side, int x, int y, int z, int heading)
	{
		super(x, y, z, heading);
		this._npcId = npcId;
		this._side = side;
	}

	public int getNpcId()
	{
		return this._npcId;
	}

	public CastleSide getSide()
	{
		return this._side;
	}
}
