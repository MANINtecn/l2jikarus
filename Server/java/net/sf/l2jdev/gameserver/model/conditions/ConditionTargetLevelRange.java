package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionTargetLevelRange extends Condition
{
	private final int[] _levels;

	public ConditionTargetLevelRange(int[] levels)
	{
		this._levels = levels;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effected == null)
		{
			return false;
		}
		int level = effected.getLevel();
		return level >= this._levels[0] && level <= this._levels[1];
	}
}
