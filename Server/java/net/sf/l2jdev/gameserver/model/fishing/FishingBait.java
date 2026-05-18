package net.sf.l2jdev.gameserver.model.fishing;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.util.Rnd;

public class FishingBait
{
	private final int _itemId;
	private final int _level;
	private final int _minPlayerLevel;
	private final int _maxPlayerLevel;
	private final double _chance;
	private final int _timeMin;
	private final int _timeMax;
	private final int _waitMin;
	private final int _waitMax;
	private final boolean _isPremiumOnly;
	private final List<FishingCatch> _rewards = new ArrayList<>();

	public FishingBait(int itemId, int level, int minPlayerLevel, int maxPlayerLevel, double chance, int timeMin, int timeMax, int waitMin, int waitMax, boolean isPremiumOnly)
	{
		this._itemId = itemId;
		this._level = level;
		this._minPlayerLevel = minPlayerLevel;
		this._maxPlayerLevel = maxPlayerLevel;
		this._chance = chance;
		this._timeMin = timeMin;
		this._timeMax = timeMax;
		this._waitMin = waitMin;
		this._waitMax = waitMax;
		this._isPremiumOnly = isPremiumOnly;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public int getLevel()
	{
		return this._level;
	}

	public int getMinPlayerLevel()
	{
		return this._minPlayerLevel;
	}

	public int getMaxPlayerLevel()
	{
		return this._maxPlayerLevel;
	}

	public double getChance()
	{
		return this._chance;
	}

	public int getTimeMin()
	{
		return this._timeMin;
	}

	public int getTimeMax()
	{
		return this._timeMax;
	}

	public int getWaitMin()
	{
		return this._waitMin;
	}

	public int getWaitMax()
	{
		return this._waitMax;
	}

	public boolean isPremiumOnly()
	{
		return this._isPremiumOnly;
	}

	public List<FishingCatch> getRewards()
	{
		return this._rewards;
	}

	public void addReward(FishingCatch catchData)
	{
		this._rewards.add(catchData);
	}

	public FishingCatch getRandom()
	{
		float random = Rnd.get(100);

		for (FishingCatch fishingCatchData : this._rewards)
		{
			if (fishingCatchData.getChance() > random)
			{
				return fishingCatchData;
			}

			random -= fishingCatchData.getChance();
		}

		return null;
	}
}
