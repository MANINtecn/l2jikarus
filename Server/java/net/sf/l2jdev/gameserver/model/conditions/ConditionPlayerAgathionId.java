package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerAgathionId extends Condition
{
	private final int _agathionId;

	public ConditionPlayerAgathionId(int agathionId)
	{
		this._agathionId = agathionId;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effector.isPlayer() && effector.asPlayer().getAgathionId() == this._agathionId;
	}
}
