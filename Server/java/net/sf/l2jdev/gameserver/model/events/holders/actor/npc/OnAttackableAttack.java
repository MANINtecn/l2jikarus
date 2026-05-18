package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Attackable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class OnAttackableAttack implements IBaseEvent
{
	private final Player _attacker;
	private final Attackable _target;
	private final int _damage;
	private final Skill _skill;
	private final boolean _isSummon;

	public OnAttackableAttack(Player attacker, Attackable target, int damage, Skill skill, boolean isSummon)
	{
		this._attacker = attacker;
		this._target = target;
		this._damage = damage;
		this._skill = skill;
		this._isSummon = isSummon;
	}

	public Player getAttacker()
	{
		return this._attacker;
	}

	public Attackable getTarget()
	{
		return this._target;
	}

	public int getDamage()
	{
		return this._damage;
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public boolean isSummon()
	{
		return this._isSummon;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_ATTACKABLE_ATTACK;
	}
}
