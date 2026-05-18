package net.sf.l2jdev.gameserver.model.actor.request;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class AutoPeelRequest extends AbstractRequest
{
	private final Item _item;
	private long _totalPeelCount;
	private long _remainingPeelCount;

	public AutoPeelRequest(Player player, Item item)
	{
		super(player);
		this._item = item;
	}

	public Item getItem()
	{
		return this._item;
	}

	public long getTotalPeelCount()
	{
		return this._totalPeelCount;
	}

	public void setTotalPeelCount(long count)
	{
		this._totalPeelCount = count;
	}

	public long getRemainingPeelCount()
	{
		return this._remainingPeelCount;
	}

	public void setRemainingPeelCount(long count)
	{
		this._remainingPeelCount = count;
	}

	@Override
	public boolean isItemRequest()
	{
		return true;
	}

	@Override
	public boolean canWorkWith(AbstractRequest request)
	{
		return !request.isItemRequest();
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return this._item.getObjectId() == objectId;
	}
}
