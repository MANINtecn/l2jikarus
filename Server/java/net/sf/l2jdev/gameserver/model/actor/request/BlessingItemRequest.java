package net.sf.l2jdev.gameserver.model.actor.request;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class BlessingItemRequest extends AbstractRequest
{
	private volatile int _blessScrollId;

	public BlessingItemRequest(Player player, int itemId)
	{
		super(player);
		this._blessScrollId = itemId;
	}

	public int getBlessScrollId()
	{
		return this._blessScrollId;
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return false;
	}
}
