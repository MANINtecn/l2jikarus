package net.sf.l2jdev.gameserver.model.actor.holders.player;

import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class SummonRequestHolder
{
	private final Player _summoner;
	private final Location _location;

	public SummonRequestHolder(Player summoner)
	{
		this._summoner = summoner;
		this._location = summoner == null ? null : new Location(summoner.getX(), summoner.getY(), summoner.getZ(), summoner.getHeading());
	}

	public Player getSummoner()
	{
		return this._summoner;
	}

	public Location getLocation()
	{
		return this._location;
	}
}
