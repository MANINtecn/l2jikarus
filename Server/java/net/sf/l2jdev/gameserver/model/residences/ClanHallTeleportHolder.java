package net.sf.l2jdev.gameserver.model.residences;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.network.NpcStringId;

public class ClanHallTeleportHolder extends Location
{
	private final NpcStringId _npcStringId;
	private final int _minFunctionLevel;
	private final int _cost;

	public ClanHallTeleportHolder(int npcStringId, int x, int y, int z, int minFunctionLevel, int cost)
	{
		super(x, y, z);
		this._npcStringId = NpcStringId.getNpcStringId(npcStringId);
		this._minFunctionLevel = minFunctionLevel;
		this._cost = cost;
	}

	public NpcStringId getNpcStringId()
	{
		return this._npcStringId;
	}

	public int getMinFunctionLevel()
	{
		return this._minFunctionLevel;
	}

	public int getCost()
	{
		return this._cost;
	}
}
