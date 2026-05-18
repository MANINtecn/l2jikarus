package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExReplyPostItemList;

public class RequestPostItemList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		if (GeneralConfig.ALLOW_MAIL && GeneralConfig.ALLOW_ATTACHMENTS)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				player.sendPacket(new ExReplyPostItemList(1, player));
				player.sendPacket(new ExReplyPostItemList(2, player));
			}
		}
	}
}
