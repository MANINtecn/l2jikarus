package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerLevel extends Condition
{
	private final int _level;

	public ConditionPlayerLevel(int level)
	{
		this._level = level;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effector.getLevel() >= this._level;
	}
}
