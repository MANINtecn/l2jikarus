package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionTargetAggro extends Condition
{
	private final boolean _isAggro;

	public ConditionTargetAggro(boolean isAggro)
	{
		this._isAggro = isAggro;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effected != null)
		{
			if (effected.isMonster())
			{
				return effected.asMonster().isAggressive() == this._isAggro;
			}

			if (effected.isPlayer())
			{
				return effected.asPlayer().getReputation() < 0;
			}
		}

		return false;
	}
}
