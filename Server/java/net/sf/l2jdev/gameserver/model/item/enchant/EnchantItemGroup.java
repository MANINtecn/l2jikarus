package net.sf.l2jdev.gameserver.model.item.enchant;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

import net.sf.l2jdev.gameserver.data.holders.RangeChanceHolder;

public class EnchantItemGroup
{
	private static final Logger LOGGER = Logger.getLogger(EnchantItemGroup.class.getName());
	private final List<RangeChanceHolder> _chances = new ArrayList<>();
	private final String _name;
	private int _maximumEnchant = -1;

	public EnchantItemGroup(String name)
	{
		this._name = name;
	}

	public String getName()
	{
		return this._name;
	}

	public void addChance(RangeChanceHolder holder)
	{
		this._chances.add(holder);
	}

	public double getChance(int index)
	{
		if (!this._chances.isEmpty())
		{
			for (RangeChanceHolder holder : this._chances)
			{
				if (holder.getMin() <= index && holder.getMax() >= index)
				{
					return holder.getChance();
				}
			}

			return this._chances.get(this._chances.size() - 1).getChance();
		}
		LOGGER.warning(this.getClass().getSimpleName() + ": item group: " + this._name + " doesn't have any chances!");
		return -1.0;
	}

	public int getMaximumEnchant()
	{
		if (this._maximumEnchant == -1)
		{
			for (RangeChanceHolder holder : this._chances)
			{
				if (holder.getChance() > 0.0 && holder.getMax() > this._maximumEnchant)
				{
					this._maximumEnchant = holder.getMax();
				}
			}

			this._maximumEnchant++;
		}

		return this._maximumEnchant;
	}
}
