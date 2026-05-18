package net.sf.l2jdev.gameserver.network.clientpackets.enchant;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.EnchantItemRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.enchant.EnchantResult;

public class RequestExCancelEnchantItem extends ClientPacket
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
			player.sendPacket(new EnchantResult(2, null, null, 0));
			player.removeRequest(EnchantItemRequest.class);
			player.getChallengeInfo().setChallengePointsPendingRecharge(-1, -1);
		}
	}
}
