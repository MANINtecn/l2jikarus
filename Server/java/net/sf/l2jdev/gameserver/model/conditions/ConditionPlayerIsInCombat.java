package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;

public class ConditionPlayerIsInCombat extends Condition
{
	private final boolean _value;

	public ConditionPlayerIsInCombat(boolean value)
	{
		this._value = value;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		boolean isInCombat = !AttackStanceTaskManager.getInstance().hasAttackStanceTask(effector);
		return this._value == isInCombat;
	}
}
