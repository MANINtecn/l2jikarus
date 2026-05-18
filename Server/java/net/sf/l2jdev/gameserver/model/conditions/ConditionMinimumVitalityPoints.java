package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionMinimumVitalityPoints extends Condition
{
	private final int _count;

	public ConditionMinimumVitalityPoints(int count)
	{
		this._count = count;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		return player != null ? player.getVitalityPoints() >= this._count : false;
	}
}
