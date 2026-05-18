package net.sf.l2jdev.gameserver.model.item.henna;

import java.util.EnumMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.combination.CombinationItemType;

public class CombinationHenna
{
	private final int _henna;
	private final int _itemOne;
	private final long _countOne;
	private final int _itemTwo;
	private final long _countTwo;
	private final long _commission;
	private final float _chance;
	private final Map<CombinationItemType, CombinationHennaReward> _rewards = new EnumMap<>(CombinationItemType.class);

	public CombinationHenna(StatSet set)
	{
		this._henna = set.getInt("dyeId");
		this._itemOne = set.getInt("itemOne", -1);
		this._countOne = set.getLong("countOne", 1L);
		this._itemTwo = set.getInt("itemTwo", -1);
		this._countTwo = set.getLong("countTwo", 1L);
		this._commission = set.getLong("commission", 0L);
		this._chance = set.getFloat("chance", 33.0F);
	}

	public int getHenna()
	{
		return this._henna;
	}

	public int getItemOne()
	{
		return this._itemOne;
	}

	public long getCountOne()
	{
		return this._countOne;
	}

	public int getItemTwo()
	{
		return this._itemTwo;
	}

	public long getCountTwo()
	{
		return this._countTwo;
	}

	public long getCommission()
	{
		return this._commission;
	}

	public float getChance()
	{
		return this._chance;
	}

	public void addReward(CombinationHennaReward item)
	{
		this._rewards.put(item.getType(), item);
	}

	public CombinationHennaReward getReward(CombinationItemType type)
	{
		return this._rewards.get(type);
	}
}
