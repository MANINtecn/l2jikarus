package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class OnCreatureSkillUse implements IBaseEvent
{
	private Creature _caster;
	private Skill _skill;
	private boolean _simultaneously;

	public Creature getCaster()
	{
		return this._caster;
	}

	public synchronized void setCaster(Creature caster)
	{
		this._caster = caster;
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public synchronized void setSkill(Skill skill)
	{
		this._skill = skill;
	}

	public boolean isSimultaneously()
	{
		return this._simultaneously;
	}

	public synchronized void setSimultaneously(boolean simultaneously)
	{
		this._simultaneously = simultaneously;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_CREATURE_SKILL_USE;
	}
}
