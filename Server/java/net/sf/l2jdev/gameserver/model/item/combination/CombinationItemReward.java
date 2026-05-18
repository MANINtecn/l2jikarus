package net.sf.l2jdev.gameserver.model.item.combination;

import net.sf.l2jdev.gameserver.model.item.holders.ItemEnchantHolder;

public class CombinationItemReward extends ItemEnchantHolder
{
	private final CombinationItemType _type;

	public CombinationItemReward(int id, int count, CombinationItemType type, int enchant)
	{
		super(id, count, enchant);
		this._type = type;
	}

	public CombinationItemType getType()
	{
		return this._type;
	}
}
