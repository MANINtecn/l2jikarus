package net.sf.l2jdev.gameserver.model.conditions;

import java.util.Set;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionTargetNpcId extends Condition
{
	private final Set<Integer> _npcIds;

	public ConditionTargetNpcId(Set<Integer> npcIds)
	{
		this._npcIds = npcIds;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return effected == null || !effected.isNpc() && !effected.isDoor() ? false : this._npcIds.contains(effected.getId());
	}
}
