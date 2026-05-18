package net.sf.l2jdev.gameserver.model.itemauction;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class ItemAuctionBid
{
	private final int _playerObjId;
	private long _lastBid;

	public ItemAuctionBid(int playerObjId, long lastBid)
	{
		this._playerObjId = playerObjId;
		this._lastBid = lastBid;
	}

	public int getPlayerObjId()
	{
		return this._playerObjId;
	}

	public long getLastBid()
	{
		return this._lastBid;
	}

	public void setLastBid(long lastBid)
	{
		this._lastBid = lastBid;
	}

	public void cancelBid()
	{
		this._lastBid = -1L;
	}

	public boolean isCanceled()
	{
		return this._lastBid <= 0L;
	}

	public Player getPlayer()
	{
		return World.getInstance().getPlayer(this._playerObjId);
	}
}
