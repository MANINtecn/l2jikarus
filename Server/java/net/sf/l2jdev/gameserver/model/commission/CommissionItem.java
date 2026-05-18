package net.sf.l2jdev.gameserver.model.commission;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.concurrent.ScheduledFuture;

import net.sf.l2jdev.gameserver.model.ItemInfo;
import net.sf.l2jdev.gameserver.model.item.instance.Item;

public class CommissionItem
{
	private final long _commissionId;
	private final Item _itemInstance;
	private final ItemInfo _itemInfo;
	private final long _pricePerUnit;
	private final Instant _startTime;
	private final byte _durationInDays;
	private final byte _discountInPercentage;
	private ScheduledFuture<?> _saleEndTask;

	public CommissionItem(long commissionId, Item itemInstance, long pricePerUnit, Instant startTime, byte durationInDays, byte discountInPercentage)
	{
		this._commissionId = commissionId;
		this._itemInstance = itemInstance;
		this._itemInfo = new ItemInfo(this._itemInstance);
		this._pricePerUnit = pricePerUnit;
		this._startTime = startTime;
		this._durationInDays = durationInDays;
		this._discountInPercentage = discountInPercentage;
	}

	public long getCommissionId()
	{
		return this._commissionId;
	}

	public Item getItemInstance()
	{
		return this._itemInstance;
	}

	public ItemInfo getItemInfo()
	{
		return this._itemInfo;
	}

	public long getPricePerUnit()
	{
		return this._pricePerUnit;
	}

	public Instant getStartTime()
	{
		return this._startTime;
	}

	public byte getDurationInDays()
	{
		return this._durationInDays;
	}

	public byte getDiscountInPercentage()
	{
		return this._discountInPercentage;
	}

	public Instant getEndTime()
	{
		return this._startTime.plus(this._durationInDays, ChronoUnit.DAYS);
	}

	public ScheduledFuture<?> getSaleEndTask()
	{
		return this._saleEndTask;
	}

	public void setSaleEndTask(ScheduledFuture<?> saleEndTask)
	{
		this._saleEndTask = saleEndTask;
	}
}
