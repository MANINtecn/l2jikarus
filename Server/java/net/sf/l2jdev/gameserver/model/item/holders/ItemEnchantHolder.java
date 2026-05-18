package net.sf.l2jdev.gameserver.model.item.holders;

import net.sf.l2jdev.gameserver.model.StatSet;

public class ItemEnchantHolder extends ItemHolder
{
	private final int _enchantLevel;

	public ItemEnchantHolder(StatSet set)
	{
		super(set);
		this._enchantLevel = 0;
	}

	public ItemEnchantHolder(int id, long count)
	{
		super(id, count);
		this._enchantLevel = 0;
	}

	public ItemEnchantHolder(int id, long count, int enchantLevel)
	{
		super(id, count);
		this._enchantLevel = enchantLevel;
	}

	public int getEnchantLevel()
	{
		return this._enchantLevel;
	}

	@Override
	public boolean equals(Object obj)
	{
		if (obj instanceof ItemEnchantHolder objInstance)
		{
			return obj == this ? true : this.getId() == objInstance.getId() && this.getCount() == objInstance.getCount() && this._enchantLevel == objInstance.getEnchantLevel();
		}
		return false;
	}

	@Override
	public String toString()
	{
		return "[" + this.getClass().getSimpleName() + "] ID: " + this.getId() + ", count: " + this.getCount() + ", enchant level: " + this._enchantLevel;
	}
}
