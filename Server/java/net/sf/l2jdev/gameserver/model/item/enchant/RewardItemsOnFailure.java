package net.sf.l2jdev.gameserver.model.item.enchant;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;

public class RewardItemsOnFailure
{
	private final Map<Integer, Map<Integer, List<ItemChanceHolder>>> _rewards = new HashMap<>();

	public void addItemToHolder(int destroyedItemId, int enchantLevel, int rewardId, long count, double chance)
	{
		ItemChanceHolder item = new ItemChanceHolder(rewardId, chance, count);
		this._rewards.computeIfAbsent(destroyedItemId, _ -> new HashMap<>()).computeIfAbsent(enchantLevel, _ -> new ArrayList<>()).add(item);
	}

	public List<ItemChanceHolder> getRewardItems(int destroyedItemId, int enchantLevel)
	{
		return this._rewards.getOrDefault(destroyedItemId, Collections.emptyMap()).getOrDefault(enchantLevel, Collections.emptyList());
	}

	public int size()
	{
		int count = 0;

		for (Map<Integer, List<ItemChanceHolder>> rewardsByEnchant : this._rewards.values())
		{
			for (List<ItemChanceHolder> rewards : rewardsByEnchant.values())
			{
				count += rewards.size();
			}
		}

		return count;
	}
}
