package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;

public class NoSummonFriendZone extends ZoneType
{
	public NoSummonFriendZone(int id)
	{
		super(id);
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
}
