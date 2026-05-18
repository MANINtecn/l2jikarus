package net.sf.l2jdev.gameserver.model.item.combination;

import java.util.EnumMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.StatSet;

public class CombinationItem
{
	private final int _itemOne;
	private final int _enchantOne;
	private final int _itemTwo;
	private final int _enchantTwo;
	private final long _commission;
	private final float _chance;
	private final boolean _announce;
	private final Map<CombinationItemType, CombinationItemReward> _rewards = new EnumMap<>(CombinationItemType.class);

	public CombinationItem(StatSet set)
	{
		this._itemOne = set.getInt("one");
		this._enchantOne = set.getInt("enchantOne", 0);
		this._itemTwo = set.getInt("two");
		this._enchantTwo = set.getInt("enchantTwo", 0);
		this._commission = set.getLong("commission", 0L);
		this._chance = set.getFloat("chance", 33.0F);
		this._announce = set.getBoolean("announce", false);
	}

	public int getItemOne()
	{
		return this._itemOne;
	}

	public int getEnchantOne()
	{
		return this._enchantOne;
	}

	public int getItemTwo()
	{
		return this._itemTwo;
	}

	public int getEnchantTwo()
	{
		return this._enchantTwo;
	}

	public long getCommission()
	{
		return this._commission;
	}

	public float getChance()
	{
		return this._chance;
	}

	public boolean isAnnounce()
	{
		return this._announce;
	}

	public void addReward(CombinationItemReward item)
	{
		this._rewards.put(item.getType(), item);
	}

	public CombinationItemReward getReward(CombinationItemType type)
	{
		return this._rewards.get(type);
	}
}
