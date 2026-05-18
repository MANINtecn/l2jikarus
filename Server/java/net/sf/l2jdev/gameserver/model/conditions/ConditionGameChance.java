package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.commons.util.Rnd;
import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionGameChance extends Condition
{
	private final int _chance;

	public ConditionGameChance(int chance)
	{
		this._chance = chance;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return Rnd.get(100) < this._chance;
	}
}
