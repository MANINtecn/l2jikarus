package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionPlayerInvSize extends Condition
{
	private final int _size;

	public ConditionPlayerInvSize(int size)
	{
		this._size = size;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		Player player = effector.asPlayer();
		return player != null ? player.getInventory().getNonQuestSize() <= player.getInventoryLimit() - this._size : true;
	}
}
