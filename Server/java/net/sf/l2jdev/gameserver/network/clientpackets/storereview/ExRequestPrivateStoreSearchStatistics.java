package net.sf.l2jdev.gameserver.network.clientpackets.storereview;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.storereview.RequestPrivateStoreSearchStatistics;

public class ExRequestPrivateStoreSearchStatistics extends ClientPacket
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
			player.sendPacket(new RequestPrivateStoreSearchStatistics());
		}
	}
}
