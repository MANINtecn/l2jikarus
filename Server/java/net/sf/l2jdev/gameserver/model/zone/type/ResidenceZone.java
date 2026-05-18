package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.zone.ZoneRespawn;

public abstract class ResidenceZone extends ZoneRespawn
{
	private int _residenceId;

	protected ResidenceZone(int id)
	{
		super(id);
	}

	public void banishForeigners(int owningClanId)
	{
		for (Player temp : this.getPlayersInside())
		{
			if (owningClanId == 0 || temp.getClanId() != owningClanId)
			{
				temp.teleToLocation(this.getBanishSpawnLoc(), true);
			}
		}
	}

	protected void setResidenceId(int residenceId)
	{
		this._residenceId = residenceId;
	}

	public int getResidenceId()
	{
		return this._residenceId;
	}
}
