package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;

public class OnCreatureAttackAvoid implements IBaseEvent
{
	private Creature _attacker;
	private Creature _target;
	private boolean _damageOverTime;

	public Creature getAttacker()
	{
		return this._attacker;
	}

	public synchronized void setAttacker(Creature attacker)
	{
		this._attacker = attacker;
	}

	public Creature getTarget()
	{
		return this._target;
	}

	public synchronized void setTarget(Creature target)
	{
		this._target = target;
	}

	public boolean isDamageOverTime()
	{
		return this._damageOverTime;
	}

	public synchronized void setDamageOverTime(boolean damageOverTime)
	{
		this._damageOverTime = damageOverTime;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_ATTACK_AVOID;
	}
}
