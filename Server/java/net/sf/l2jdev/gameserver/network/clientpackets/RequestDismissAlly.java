package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestDismissAlly extends ClientPacket
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
			if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ACCESS_ONLY_FOR_THE_CHANNEL_FOUNDER);
			}
			else
			{
				player.getClan().dissolveAlly(player);
			}
		}
	}
}
