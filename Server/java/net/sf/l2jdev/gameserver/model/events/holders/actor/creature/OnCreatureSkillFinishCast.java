package net.sf.l2jdev.gameserver.model.events.holders.actor.creature;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class OnCreatureSkillFinishCast implements IBaseEvent
{
	private Creature _caster;
	private WorldObject _target;
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

	public WorldObject getTarget()
	{
		return this._target;
	}

	public synchronized void setTarget(WorldObject target)
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
		return EventType.ON_CREATURE_SKILL_FINISH_CAST;
	}
}
