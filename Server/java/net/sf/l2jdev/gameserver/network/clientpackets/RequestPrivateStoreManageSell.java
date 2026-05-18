package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;

public class RequestPrivateStoreManageSell extends ClientPacket
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
			if (player.isAlikeDead() || player.isInOlympiadMode())
			{
				player.sendPacket(ActionFailed.STATIC_PACKET);
			}
		}
	}
}
