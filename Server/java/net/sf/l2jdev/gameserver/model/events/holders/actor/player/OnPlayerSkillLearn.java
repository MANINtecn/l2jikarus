package net.sf.l2jdev.gameserver.model.events.holders.actor.player;

import net.sf.l2jdev.gameserver.model.actor.Npc;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.events.EventType;
import net.sf.l2jdev.gameserver.model.events.holders.IBaseEvent;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.model.skill.enums.AcquireSkillType;

public class OnPlayerSkillLearn implements IBaseEvent
{
	private final Npc _trainer;
	private final Player _player;
	private final Skill _skill;
	private final AcquireSkillType _type;

	public OnPlayerSkillLearn(Npc trainer, Player player, Skill skill, AcquireSkillType type)
	{
		this._trainer = trainer;
		this._player = player;
		this._skill = skill;
		this._type = type;
	}

	public Npc getTrainer()
	{
		return this._trainer;
	}

	public Player getPlayer()
	{
		return this._player;
	}

	public Skill getSkill()
	{
		return this._skill;
	}

	public AcquireSkillType getAcquireType()
	{
		return this._type;
	}

	@Override
	public EventType getType()
	{
		return EventType.ON_PLAYER_SKILL_LEARN;
	}
}
