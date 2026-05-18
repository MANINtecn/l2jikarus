package net.sf.l2jdev.gameserver.model.item.holders;

import net.sf.l2jdev.gameserver.model.item.enums.UniqueGachaRank;

public class GachaItemTimeStampHolder extends GachaItemHolder
{
	private final long _timeStamp;
	private boolean _storedInDatabase;

	public GachaItemTimeStampHolder(int itemId, long itemCount, int enchantLevel, UniqueGachaRank rank, long timeStamp, boolean stored)
	{
		super(itemId, itemCount, 0, enchantLevel, rank);
		this._timeStamp = timeStamp;
		this._storedInDatabase = stored;
	}

	public long getTimeStamp()
	{
		return this._timeStamp;
	}

	public int getTimeStampFromNow()
	{
		long timeOfReceive = this._timeStamp / 1000L;
		long currentTime = System.currentTimeMillis() / 1000L;
		return (int) (currentTime - timeOfReceive);
	}

	public boolean getStoredStatus()
	{
		if (this._storedInDatabase)
		{
			return true;
		}
		this._storedInDatabase = true;
		return false;
	}
}
