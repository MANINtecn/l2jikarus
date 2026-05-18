package net.sf.l2jdev.gameserver.model.item.holders;

import java.util.Date;

public class ItemPenaltyHolder
{
	private final int _itemObjectId;
	private final int _killerObjectId;
	private final Date _dateLost;

	public ItemPenaltyHolder(int itemObjectId, int killerObjectId, Date dateLost)
	{
		this._itemObjectId = itemObjectId;
		this._killerObjectId = killerObjectId;
		this._dateLost = dateLost;
	}

	public int getItemObjectId()
	{
		return this._itemObjectId;
	}

	public int getKillerObjectId()
	{
		return this._killerObjectId;
	}

	public Date getDateLost()
	{
		return this._dateLost;
	}
}
