package net.sf.l2jdev.gameserver.network.clientpackets.enchant.multi;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExRequestFinishMultiEnchantScroll extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (player.getRequest(EnchantItemRequest.class) != null)
			{
				player.removeRequest(EnchantItemRequest.class);
				player.getChallengeInfo().setChallengePointsPendingRecharge(-1, -1);
			}
		}
	}
}
