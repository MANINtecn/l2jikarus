package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionTargetWeight extends Condition
{
	private final int _weight;

	public ConditionTargetWeight(int weight)
	{
		this._weight = weight;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effected != null && effected.isPlayer())
		{
			Player target = effected.asPlayer();
			if (!target.getDietMode() && target.getMaxLoad() > 0)
			{
				return (target.getCurrentLoad() - target.getBonusWeightPenalty()) * 100 / target.getMaxLoad() < this._weight;
			}
		}

		return false;
	}
}
