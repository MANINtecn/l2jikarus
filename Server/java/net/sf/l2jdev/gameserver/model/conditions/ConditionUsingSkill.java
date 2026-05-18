package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionUsingSkill extends Condition
{
	private final int _skillId;

	public ConditionUsingSkill(int skillId)
	{
		this._skillId = skillId;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return skill == null ? false : skill.getId() == this._skillId;
	}
}
