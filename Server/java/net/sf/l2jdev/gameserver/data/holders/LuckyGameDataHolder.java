package net.sf.l2jdev.gameserver.data.holders;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.gameserver.model.StatSet;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.model.item.holders.ItemPointHolder;

public class LuckyGameDataHolder
{
	private final int _index;
	private final int _turningPoints;
	private final List<ItemChanceHolder> _commonRewards = new ArrayList<>();
	private final List<ItemPointHolder> _uniqueRewards = new ArrayList<>();
	private final List<ItemChanceHolder> _modifyRewards = new ArrayList<>();
	private int _minModifyRewardGame;
	private int _maxModifyRewardGame;

	public LuckyGameDataHolder(StatSet params)
	{
		this._index = params.getInt("index");
		this._turningPoints = params.getInt("turning_point");
	}

	public void addCommonReward(ItemChanceHolder item)
	{
		this._commonRewards.add(item);
	}

	public void addUniqueReward(ItemPointHolder item)
	{
		this._uniqueRewards.add(item);
	}

	public void addModifyReward(ItemChanceHolder item)
	{
		this._modifyRewards.add(item);
	}

	public List<ItemChanceHolder> getCommonReward()
	{
		return this._commonRewards;
	}

	public List<ItemPointHolder> getUniqueReward()
	{
		return this._uniqueRewards;
	}

	public List<ItemChanceHolder> getModifyReward()
	{
		return this._modifyRewards;
	}

	public void setMinModifyRewardGame(int minModifyRewardGame)
	{
		this._minModifyRewardGame = minModifyRewardGame;
	}

	public void setMaxModifyRewardGame(int maxModifyRewardGame)
	{
		this._maxModifyRewardGame = maxModifyRewardGame;
	}

	public int getMinModifyRewardGame()
	{
		return this._minModifyRewardGame;
	}

	public int getMaxModifyRewardGame()
	{
		return this._maxModifyRewardGame;
	}

	public int getIndex()
	{
		return this._index;
	}

	public int getTurningPoints()
	{
		return this._turningPoints;
	}
}
