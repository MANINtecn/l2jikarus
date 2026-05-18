package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerTransformationId extends Condition
{
	private final int _id;

	public ConditionPlayerTransformationId(int id)
	{
		this._id = id;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return this._id == -1 ? effector.isTransformed() : effector.getTransformationId() == this._id;
	}
}
