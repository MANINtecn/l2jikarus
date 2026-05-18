package net.sf.l2jdev.gameserver.cache;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import net.sf.l2jdev.gameserver.data.xml.ArmorSetData;
import net.sf.l2jdev.gameserver.model.ArmorSet;
import net.sf.l2jdev.gameserver.model.actor.Playable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.model.stats.BaseStat;
import net.sf.l2jdev.gameserver.model.stats.Stat;

public class PaperdollCache
{
	private final Set<Item> _paperdollItems = ConcurrentHashMap.newKeySet();
	private final Map<BaseStat, Double> _baseStatValues = new ConcurrentHashMap<>();
	private final Map<Stat, Double> _statValues = new ConcurrentHashMap<>();
	private int _armorSetEnchant = -1;

	public Set<Item> getPaperdollItems()
	{
		return this._paperdollItems;
	}

	public void clearCachedStats()
	{
		this._baseStatValues.clear();
		this._statValues.clear();
		this.clearArmorSetEnchant();
	}

	public void clearArmorSetEnchant()
	{
		this._armorSetEnchant = -1;
	}

	public double getBaseStatValue(Player player, BaseStat stat)
	{
		Double baseStatValue = this._baseStatValues.get(stat);
		if (baseStatValue != null)
		{
			return baseStatValue;
		}
		Set<ArmorSet> appliedSets = new HashSet<>(2);
		double value = 0.0;

		for (Item item : this._paperdollItems)
		{
			for (ArmorSet set : ArmorSetData.getInstance().getSets(item.getId()))
			{
				if (set.getPieceCount(player) >= set.getMinimumPieces() && appliedSets.add(set))
				{
					value += set.getStatsBonus(stat);
				}
			}
		}

		this._baseStatValues.put(stat, value);
		return value;
	}

	public int getArmorSetEnchant(Playable playable)
	{
		int armorSetEnchant = this._armorSetEnchant;
		if (armorSetEnchant >= 0)
		{
			return armorSetEnchant;
		}
		armorSetEnchant = 0;

		for (Item item : this._paperdollItems)
		{
			for (ArmorSet set : ArmorSetData.getInstance().getSets(item.getId()))
			{
				int enchantEffect = set.getSetEnchant(playable);
				if (enchantEffect > armorSetEnchant)
				{
					armorSetEnchant = enchantEffect;
				}
			}
		}

		this._armorSetEnchant = armorSetEnchant;
		return armorSetEnchant;
	}

	public double getStats(Stat stat)
	{
		Double statValue = this._statValues.get(stat);
		if (statValue != null)
		{
			return statValue;
		}
		double value = 0.0;

		for (Item item : this._paperdollItems)
		{
			value += item.getTemplate().getStats(stat, 0.0);
		}

		this._statValues.put(stat, value);
		return value;
	}
}
