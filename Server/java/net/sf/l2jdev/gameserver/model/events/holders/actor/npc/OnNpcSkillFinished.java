package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class OnNpcSkillFinished implements IBaseEvent
{
	private final Npc _caster;
	private final Player _target;
	private final Skill _skill;

	public OnNpcSkillFinished(Npc caster, Player target, Skill skill)
	{
		this._caster = caster;
		this._target = target;
		this._skill = skill;
	}

	public Player getTarget()
	{
		return this._target;
	}

	public Npc getCaster()
	{
		return this._caster;
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_SKILL_FINISHED;
	}
}
