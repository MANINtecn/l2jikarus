package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerCheckAbnormal extends Condition
{
	private final AbnormalType _type;
	private final int _level;

	public ConditionPlayerCheckAbnormal(AbnormalType type)
	{
		this._type = type;
		this._level = -1;
	}

	public ConditionPlayerCheckAbnormal(AbnormalType type, int level)
	{
		this._type = type;
		this._level = level;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return this._level == -1 ? effector.getEffectList().hasAbnormalType(this._type) : effector.getEffectList().hasAbnormalType(this._type, info -> this._level >= info.getSkill().getAbnormalLevel());
	}
}
