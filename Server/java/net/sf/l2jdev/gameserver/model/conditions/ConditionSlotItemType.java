package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionSlotItemType extends ConditionInventory
{
	private final int _mask;

	public ConditionSlotItemType(int slot, int mask)
	{
		super(slot);
		this._mask = mask;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector != null && effector.isPlayer())
		{
			Item itemSlot = effector.getInventory().getPaperdollItem(this._slot);
			return itemSlot == null ? false : (itemSlot.getTemplate().getItemMask() & this._mask) != 0;
		}
		return false;
	}
}
