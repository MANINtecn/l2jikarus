package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneRespawn;

public class ResidenceTeleportZone extends ZoneRespawn
{
	private int _residenceId;

	public ResidenceTeleportZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("residenceId"))
		{
			this._residenceId = Integer.parseInt(value);
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature creature)
	{
		creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, true);
	}

	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.NO_SUMMON_FRIEND, false);
	}

	@Override
	public void oustAllPlayers()
	{
		for (Player player : this.getPlayersInside())
		{
			if (player != null && player.isOnline())
			{
				player.teleToLocation(this.getSpawnLoc(), 200);
			}
		}
	}

	public int getResidenceId()
	{
		return this._residenceId;
	}
}
