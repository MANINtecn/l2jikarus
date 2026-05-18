package net.sf.l2jdev.gameserver.model.events.holders.actor.npc;

import java.util.Collection;
import java.util.Collections;

import net.sf.l2jdev.gameserver.model.WorldObject;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class OnNpcSkillSee implements IBaseEvent
{
	private final Npc _npc;
	private final Player _caster;
	private final Skill _skill;
	private final Collection<WorldObject> _targets;
	private final boolean _isSummon;

	public OnNpcSkillSee(Npc npc, Player caster, Skill skill, boolean isSummon, Collection<WorldObject> targets)
	{
		this._npc = npc;
		this._caster = caster;
		this._skill = skill;
		this._isSummon = isSummon;
		this._targets = targets;
	}

	public OnNpcSkillSee(Npc npc, Player caster, Skill skill, boolean isSummon, Creature target)
	{
		this._npc = npc;
		this._caster = caster;
		this._skill = skill;
		this._isSummon = isSummon;
		this._targets = Collections.singleton(target);
	}

	public Npc getTarget()
	{
		return this._npc;
	}

	public Player getCaster()
	{
		return this._caster;
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public Collection<WorldObject> getTargets()
	{
		return this._targets;
	}

	public boolean isSummon()
	{
		return this._isSummon;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_NPC_SKILL_SEE;
	}
}
