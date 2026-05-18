package net.sf.l2jdev.gameserver.network.clientpackets.compound;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.CompoundRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestNewEnchantClose extends ClientPacket
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
			player.removeRequest(CompoundRequest.class);
		}
	}
}
