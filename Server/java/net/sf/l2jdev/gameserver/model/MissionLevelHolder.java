package net.sf.l2jdev.gameserver.model;

import java.util.HashMap;
import java.util.Map;

import net.sf.l2jdev.gameserver.model.item.holders.ItemHolder;

public class MissionLevelHolder
{
	private int _maxLevel;
	private final int _bonusLevel;
	private final Map<Integer, Integer> _xpForLevel = new HashMap<>();
	private final Map<Integer, ItemHolder> _normalReward = new HashMap<>();
	private final Map<Integer, ItemHolder> _keyReward = new HashMap<>();
	private final ItemHolder _specialReward;
	private final ItemHolder _bonusReward;
	private final boolean _bonusRewardIsAvailable;
	private final boolean _bonusRewardByLevelUp;

	public MissionLevelHolder(int maxLevel, int bonusLevel, Map<Integer, Integer> xpForLevel, Map<Integer, ItemHolder> normalReward, Map<Integer, ItemHolder> keyReward, ItemHolder specialReward, ItemHolder bonusReward, boolean bonusRewardByLevelUp, boolean bonusRewardIsAvailable)
	{
		this._maxLevel = maxLevel;
		this._bonusLevel = bonusLevel;
		this._xpForLevel.putAll(xpForLevel);
		this._normalReward.putAll(normalReward);
		this._keyReward.putAll(keyReward);
		this._specialReward = specialReward;
		this._bonusReward = bonusReward;
		this._bonusRewardByLevelUp = bonusRewardByLevelUp;
		this._bonusRewardIsAvailable = bonusRewardIsAvailable;
	}

	public int getMaxLevel()
	{
		return this._maxLevel;
	}

	public void setMaxLevel(int maxLevel)
	{
		this._maxLevel = maxLevel;
	}

	public int getBonusLevel()
	{
		return this._bonusLevel;
	}

	public Map<Integer, Integer> getXPForLevel()
	{
		return this._xpForLevel;
	}

	public int getXPForSpecifiedLevel(int level)
	{
		return this._xpForLevel.get(level == 0 ? level + 1 : level);
	}

	public Map<Integer, ItemHolder> getNormalRewards()
	{
		return this._normalReward;
	}

	public Map<Integer, ItemHolder> getKeyRewards()
	{
		return this._keyReward;
	}

	public ItemHolder getSpecialReward()
	{
		return this._specialReward;
	}

	public ItemHolder getBonusReward()
	{
		return this._bonusReward;
	}

	public boolean getBonusRewardByLevelUp()
	{
		return this._bonusRewardByLevelUp;
	}

	public boolean getBonusRewardIsAvailable()
	{
		return this._bonusRewardIsAvailable;
	}
}
