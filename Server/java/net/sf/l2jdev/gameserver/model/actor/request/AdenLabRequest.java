package net.sf.l2jdev.gameserver.model.actor.request;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class AdenLabRequest extends AbstractRequest
{
	public AdenLabRequest(Player player)
	{
		super(player);
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return false;
	}
}
