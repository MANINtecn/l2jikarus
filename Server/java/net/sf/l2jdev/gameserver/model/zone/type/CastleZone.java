package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;

public class CastleZone extends ResidenceZone
{
	public CastleZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("castleId"))
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
		creature.setInsideZone(ZoneId.CASTLE, true);
	}

	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.CASTLE, false);
	}
}
