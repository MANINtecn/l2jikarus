package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;

public class FortZone extends ResidenceZone
{
	public FortZone(int id)
	{
		super(id);
	}

	@Override
	public void setParameter(String name, String value)
	{
		if (name.equals("fortId"))
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
		creature.setInsideZone(ZoneId.FORT, true);
	}

	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.FORT, false);
	}
}
