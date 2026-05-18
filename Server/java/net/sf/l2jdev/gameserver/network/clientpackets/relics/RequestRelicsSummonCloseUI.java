package net.sf.l2jdev.gameserver.network.clientpackets.relics;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.RelicSummonRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestRelicsSummonCloseUI extends ClientPacket
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
			player.removeRequest(RelicSummonRequest.class);
		}
	}
}
