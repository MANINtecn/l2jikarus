package net.sf.l2jdev.gameserver.model.zone.type;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class SiegableHallZone extends ClanHallZone
{
	private List<Location> _challengerLocations;

	public SiegableHallZone(int id)
	{
		super(id);
	}

	@Override
	public void parseLoc(int x, int y, int z, String type)
	{
		if (type != null && type.equals("challenger"))
		{
			if (this._challengerLocations == null)
			{
				this._challengerLocations = new ArrayList<>();
			}

			this._challengerLocations.add(new Location(x, y, z));
		}
		else
		{
			super.parseLoc(x, y, z, type);
		}
	}

	public List<Location> getChallengerSpawns()
	{
		return this._challengerLocations;
	}

	public void banishNonSiegeParticipants()
	{
		for (Player player : this.getPlayersInside())
		{
			if (player != null && player.isInHideoutSiege())
			{
				player.teleToLocation(this.getBanishSpawnLoc(), true);
			}
		}
	}
}
