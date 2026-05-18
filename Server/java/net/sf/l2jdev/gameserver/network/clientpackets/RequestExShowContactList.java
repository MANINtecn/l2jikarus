package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.GeneralConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExShowContactList;

public class RequestExShowContactList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		if (GeneralConfig.ALLOW_MAIL)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				player.sendPacket(new ExShowContactList(player));
			}
		}
	}
}
