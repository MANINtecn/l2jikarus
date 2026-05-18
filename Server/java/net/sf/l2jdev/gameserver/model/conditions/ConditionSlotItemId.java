package net.sf.l2jdev.gameserver.model.conditions;

import net.sf.l2jdev.gameserver.model.actor.Creature;
import net.sf.l2jdev.gameserver.model.item.ItemTemplate;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.skill.Skill;

public class ConditionSlotItemId extends ConditionInventory
{
	private final int _itemId;
	private final int _enchantLevel;

	public ConditionSlotItemId(int slot, int itemId, int enchantLevel)
	{
		super(slot);
		this._itemId = itemId;
		this._enchantLevel = enchantLevel;
	}

	@Override
	public boolean testImpl(Creature effector, Creature effected, Skill skill, ItemTemplate item)
	{
		if (effector != null && effector.isPlayer())
		{
			Item itemSlot = effector.getInventory().getPaperdollItem(this._slot);
			return itemSlot == null ? this._itemId == 0 : itemSlot.getId() == this._itemId && itemSlot.getEnchantLevel() >= this._enchantLevel;
		}
		return false;
	}
}
