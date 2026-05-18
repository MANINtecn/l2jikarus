package net.sf.l2jdev.gameserver.model.actor.request;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class PrimeShopRequest extends AbstractRequest
{
	public PrimeShopRequest(Player player)
	{
		super(player);
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return false;
	}
}
