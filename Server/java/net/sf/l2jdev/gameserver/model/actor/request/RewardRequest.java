package net.sf.l2jdev.gameserver.model.actor.request;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class RewardRequest extends AbstractRequest
{
	public RewardRequest(Player player)
	{
		super(player);
	}

	@Override
	public boolean isUsing(int objectId)
	{
		return false;
	}
}
