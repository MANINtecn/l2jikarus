package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.AbnormalType;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionTargetAbnormalType extends Condition
{
	private final AbnormalType _abnormalType;

	public ConditionTargetAbnormalType(AbnormalType abnormalType)
	{
		this._abnormalType = abnormalType;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effected.hasAbnormalType(this._abnormalType);
	}
}
