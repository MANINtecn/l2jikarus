package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.data.xml.ClanHallData;
import net.sf.l2jdev.gameserver.model.Location;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.residences.ClanHall;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;

public class ClanHallZone extends ResidenceZone
{
	public ClanHallZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("clanHallId"))
		{
			this.setResidenceId(Integer.parseInt(value));
		}
		else
		{
			super.setParameter(name, value);
		}
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.CLAN_HALL, true);
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayer())
		{
			creature.setInsideZone(ZoneId.CLAN_HALL, false);
		}
	}

	@Override
	public Location getBanishSpawnLoc()
	{
		ClanHall clanHall = ClanHallData.getInstance().getClanHallById(this.getResidenceId());
		return clanHall == null ? null : clanHall.getBanishLocation();
	}
}
