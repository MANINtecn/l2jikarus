package net.sf.l2jdev.gameserver.model.item.enchant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import net.sf.l2jdev.gameserver.model.item.ItemTemplate;

public class EnchantScrollGroup
{
	private final int _id;
	private List<EnchantRateItem> _rateGroups;

	public EnchantScrollGroup(int id)
	{
		this._id = id;
	}

	public int getId()
	{
		return this._id;
	}

	public void addRateGroup(EnchantRateItem group)
	{
		if (this._rateGroups == null)
		{
			this._rateGroups = new ArrayList<>();
		}

		this._rateGroups.add(group);
	}

	public List<EnchantRateItem> getRateGroups()
	{
		return this._rateGroups != null ? this._rateGroups : Collections.emptyList();
	}

	public EnchantRateItem getRateGroup(ItemTemplate item)
	{
		for (EnchantRateItem group : this.getRateGroups())
		{
			if (group.validate(item))
			{
				return group;
			}
		}

		return null;
	}
}
