package net.sf.l2jdev.gameserver.model.item.holders;

import java.util.List;
import java.util.Set;

import net.sf.l2jdev.commons.util.Rnd;

public class ItemChanceHolder extends ItemHolder
{
	private final double _chance;
	private final byte _enchantmentLevel;
	private final boolean _maintainIngredient;
	private Set<Integer> _targetIds = null;

	public ItemChanceHolder(int id, double chance)
	{
		this(id, chance, 1L);
	}

	public ItemChanceHolder(int id, double chance, long count)
	{
		super(id, count);
		this._chance = chance;
		this._enchantmentLevel = 0;
		this._maintainIngredient = false;
		this._targetIds = null;
	}

	public ItemChanceHolder(int id, double chance, long count, byte enchantmentLevel)
	{
		super(id, count);
		this._chance = chance;
		this._enchantmentLevel = enchantmentLevel;
		this._maintainIngredient = false;
		this._targetIds = null;
	}

	public ItemChanceHolder(int id, double chance, long count, byte enchantmentLevel, boolean maintainIngredient)
	{
		super(id, count);
		this._chance = chance;
		this._enchantmentLevel = enchantmentLevel;
		this._maintainIngredient = maintainIngredient;
		this._targetIds = null;
	}

	public ItemChanceHolder(int id, long count, double chance, String[] targetIds)
	{
		super(id, count);
		this._chance = chance;
		this._enchantmentLevel = 0;
		this._maintainIngredient = false;
		if (targetIds != null)
		{
			for (String s : targetIds)
			{
				try
				{
					this._targetIds.add(Integer.parseInt(s.trim()));
				}
				catch (NumberFormatException var12)
				{
				}
			}
		}
	}

	public double getChance()
	{
		return this._chance;
	}

	public byte getEnchantmentLevel()
	{
		return this._enchantmentLevel;
	}

	public boolean isMaintainIngredient()
	{
		return this._maintainIngredient;
	}

	public boolean matches(int destroyedItemId)
	{
		return this._targetIds.isEmpty() ? true : this._targetIds.contains(destroyedItemId);
	}

	public static ItemChanceHolder getRandomHolder(List<ItemChanceHolder> holders)
	{
		double itemRandom = 100.0 * Rnd.nextDouble();

		for (ItemChanceHolder holder : holders)
		{
			if (!Double.isNaN(holder.getChance()))
			{
				if (holder.getChance() > itemRandom)
				{
					return holder;
				}

				itemRandom -= holder.getChance();
			}
		}

		return null;
	}

	@Override
	public String toString()
	{
		return "[" + this.getClass().getSimpleName() + "] ID: " + this.getId() + ", count: " + this.getCount() + ", chance: " + this._chance;
	}
}
