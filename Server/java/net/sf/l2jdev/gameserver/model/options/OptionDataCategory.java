package net.sf.l2jdev.gameserver.model.options;

import java.util.Map;
import java.util.Map.Entry;
import java.util.Set;

import net.sf.l2jdev.commons.util.Rnd;

public class OptionDataCategory
{
	private final Map<Options, Double> _options;
	private final Set<Integer> _itemIds;
	private final double _chance;
	private final boolean _isEmpty;

	public OptionDataCategory(Map<Options, Double> options, Set<Integer> itemIds, double chance)
	{
		this._options = options;
		this._itemIds = itemIds;
		this._chance = chance;
		this._isEmpty = options == null || options.isEmpty();
	}

	public boolean isEmptyCategory()
	{
		return this._isEmpty;
	}

	Options getRandomOptions()
	{
		if (this._isEmpty)
		{
			return null;
		}
		double random = Rnd.nextDouble() * 100.0;

		for (Entry<Options, Double> entry : this._options.entrySet())
		{
			if (entry.getValue() >= random)
			{
				return entry.getKey();
			}

			random -= entry.getValue();
		}

		return null;
	}

	public Map<Options, Double> getOptions()
	{
		return this._options;
	}

	public Set<Integer> getItemIds()
	{
		return this._itemIds;
	}

	public double getChance()
	{
		return this._chance;
	}
}
