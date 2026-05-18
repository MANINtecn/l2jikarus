package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;

public class DerbyTrackZone extends ZoneType
{
	public DerbyTrackZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayable())
		{
			creature.setInsideZone(ZoneId.MONSTER_TRACK, true);
		}
	}

	@Override
	protected void onExit(Creature creature)
	{
		if (creature.isPlayable())
		{
			creature.setInsideZone(ZoneId.MONSTER_TRACK, false);
		}
	}
}
