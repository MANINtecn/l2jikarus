package net.sf.l2jdev.gameserver.model.actor.holders.player;

public class CrossEventRegularRewardHolder
{
	private final int _cellId;
	private final int _rewardId;
	private final int _rewardAmount;

	public CrossEventRegularRewardHolder(int cellId, int rewardId, int rewardAmount)
	{
		this._cellId = cellId;
		this._rewardId = rewardId;
		this._rewardAmount = rewardAmount;
	}

	public int cellId()
	{
		return this._cellId;
	}

	public int cellReward()
	{
		return this._rewardId;
	}

	public int cellAmount()
	{
		return this._rewardAmount;
	}
}
