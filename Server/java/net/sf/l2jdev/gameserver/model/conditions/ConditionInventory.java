package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public abstract class ConditionInventory extends Condition
{
	protected final int _slot;

	public ConditionInventory(int slot)
	{
		this._slot = slot;
	}

	@Override
	public abstract boolean testImpl(Creature var1, Creature var2, Skill var3, ItemTemplate var4);
}
