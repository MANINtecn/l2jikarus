package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerHp extends Condition
{
	private final int _hp;

	public ConditionPlayerHp(int hp)
	{
		this._hp = hp;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effector != null && effector.getCurrentHp() * 100.0 / effector.getMaxHp() <= this._hp;
	}
}
