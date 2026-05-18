package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class OnCreatureAttacked implements IBaseEvent
{
	private Creature _attacker;
	private Creature _target;
	private Skill _skill;

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

	public Skill getSkill()
	{
		return this._skill;
	}

	public synchronized void setSkill(Skill skill)
	{
		this._skill = skill;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_ATTACKED;
	}
}
