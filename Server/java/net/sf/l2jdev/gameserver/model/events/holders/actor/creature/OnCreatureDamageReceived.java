package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class OnCreatureDamageReceived implements IBaseEvent
{
	private Creature _attacker;
	private Creature _target;
	private double _damage;
	private Skill _skill;
	private boolean _crit;
	private boolean _damageOverTime;
	private boolean _reflect;

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

	public double getDamage()
	{
		return this._damage;
	}

	public synchronized void setDamage(double damage)
	{
		this._damage = damage;
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public synchronized void setSkill(Skill skill)
	{
		this._skill = skill;
	}

	public boolean isCritical()
	{
		return this._crit;
	}

	public synchronized void setCritical(boolean crit)
	{
		this._crit = crit;
	}

	public boolean isDamageOverTime()
	{
		return this._damageOverTime;
	}

	public synchronized void setDamageOverTime(boolean damageOverTime)
	{
		this._damageOverTime = damageOverTime;
	}

	public boolean isReflect()
	{
		return this._reflect;
	}

	public synchronized void setReflect(boolean reflect)
	{
		this._reflect = reflect;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_DAMAGE_RECEIVED;
	}
}
