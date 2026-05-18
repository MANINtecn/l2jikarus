package net.sf.l2jdev.gameserver.model.options;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;

public class OptionDataGroup
{
	private final int _order;
	private final List<OptionDataCategory> _categories;

	public OptionDataGroup(int order, List<OptionDataCategory> categories)
	{
		this._order = order;
		this._categories = categories;
	}

	public int getOrder()
	{
		return this._order;
	}

	public List<OptionDataCategory> getCategories()
	{
		return this._categories;
	}

	public Options getRandomEffect(int itemId)
	{
		List<OptionDataCategory> exclusions = new ArrayList<>();
		Options result = null;

		do
		{
			double random = Rnd.nextDouble() * 100.0;

			for (OptionDataCategory category : this._categories)
			{
				if (category.getItemIds().isEmpty() || category.getItemIds().contains(itemId))
				{
					double chance = category.getChance();
					if (random < chance)
					{
						if (category.isEmptyCategory())
						{
							return null;
						}

						return category.getRandomOptions();
					}

					random -= chance;
				}
				else if (!exclusions.contains(category))
				{
					exclusions.add(category);
				}
			}
		}
		while (exclusions.size() < this._categories.size());

		return result;
	}
}
