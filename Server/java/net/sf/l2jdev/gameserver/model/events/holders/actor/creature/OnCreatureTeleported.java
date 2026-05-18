package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnCreatureTeleported implements IBaseEvent
{
	private final Creature _creature;

	public OnCreatureTeleported(Creature creature)
	{
		this._creature = creature;
	}

	public Creature getCreature()
	{
		return this._creature;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_TELEPORTED;
	}
}
