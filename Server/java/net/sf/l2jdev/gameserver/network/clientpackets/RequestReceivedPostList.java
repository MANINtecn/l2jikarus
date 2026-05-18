package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowReceivedPostList;

public class RequestReceivedPostList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && GeneralConfig.ALLOW_MAIL)
		{
			player.sendPacket(new ExShowReceivedPostList(player.getObjectId()));
		}
	}
}
