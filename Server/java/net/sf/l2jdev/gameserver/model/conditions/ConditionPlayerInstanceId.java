package net.sf.l2jdev.gameserver.model.conditions;

import java.util.Set;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.instancezone.Instance;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerInstanceId extends Condition
{
	private final Set<Integer> _instanceIds;

	public ConditionPlayerInstanceId(Set<Integer> instanceIds)
	{
		this._instanceIds = instanceIds;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		if (player == null)
		{
			return false;
		}
		Instance instance = player.getInstanceWorld();
		return instance != null && this._instanceIds.contains(instance.getTemplateId());
	}
}
