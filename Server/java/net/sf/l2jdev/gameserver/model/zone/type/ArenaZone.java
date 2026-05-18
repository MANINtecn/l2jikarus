package net.sf.l2jdev.gameserver.model.zone.type;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.zone.ZoneId;
import net.sf.l2jdev.gameserver.model.zone.ZoneType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class ArenaZone extends ZoneType
{
	public ArenaZone(int id)
	{
		super(id);
	}

	@Override
	protected void onEnter(Creature creature)
	{
		if (creature.isPlayer() && !creature.isInsideZone(ZoneId.PVP))
		{
			creature.sendPacket(SystemMessageId.YOU_HAVE_ENTERED_A_COMBAT_ZONE);
		}

		creature.setInsideZone(ZoneId.PVP, true);
	}

	@Override
	protected void onExit(Creature creature)
	{
		creature.setInsideZone(ZoneId.PVP, false);
		if (creature.isPlayer() && !creature.isInsideZone(ZoneId.PVP))
		{
			creature.sendPacket(SystemMessageId.YOU_HAVE_LEFT_A_COMBAT_ZONE);
		}
	}
}
