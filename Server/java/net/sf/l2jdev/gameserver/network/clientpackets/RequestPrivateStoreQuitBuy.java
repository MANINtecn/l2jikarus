package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.enums.player.PrivateStoreType;

public class RequestPrivateStoreQuitBuy extends ClientPacket
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
			player.setPrivateStoreType(PrivateStoreType.NONE);
			player.standUp();
			player.broadcastUserInfo();
		}
	}
}
