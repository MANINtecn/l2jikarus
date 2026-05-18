package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnCreatureDeath implements IBaseEvent
{
	private final Creature _attacker;
	private final Creature _target;

	public OnCreatureDeath(Creature attacker, Creature target)
	{
		this._attacker = attacker;
		this._target = target;
	}

	public Creature getAttacker()
	{
		return this._attacker;
	}

	public Creature getTarget()
	{
		return this._target;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_DEATH;
	}
}
