package net.sf.l2jdev.gameserver.data.holders;

import java.util.concurrent.atomic.AtomicInteger;

public class LimitShopRandomCraftReward
{
	private final int _itemId;
	private final AtomicInteger _count;
	private final int _rewardIndex;

	public LimitShopRandomCraftReward(int itemId, int count, int rewardIndex)
	{
		this._itemId = itemId;
		this._count = new AtomicInteger(count);
		this._rewardIndex = rewardIndex;
	}

	public int getItemId()
	{
		return this._itemId;
	}

	public AtomicInteger getCount()
	{
		return this._count;
	}

	public int getRewardIndex()
	{
		return this._rewardIndex;
	}
}
