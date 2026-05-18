package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionItemId extends Condition
{
	private final int _itemId;

	public ConditionItemId(int itemId)
	{
		this._itemId = itemId;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		return item != null && item.getId() == this._itemId;
	}
}
