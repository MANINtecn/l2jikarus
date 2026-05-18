package net.sf.l2jdev.gameserver.network.clientpackets.blessing;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.BlessingItemRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.blessing.ExBlessOptionCancel;

public class RequestBlessOptionCancel extends ClientPacket
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
			player.removeRequest(BlessingItemRequest.class);
			player.sendPacket(new ExBlessOptionCancel(1));
		}
	}
}
