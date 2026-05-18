package net.sf.l2jdev.gameserver.network.clientpackets.secretshop;

import net.sf.l2jdev.gameserver.managers.events.SecretShopEventManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExRequestFestivalBmGame extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			SecretShopEventManager.getInstance().addTickets(player);
		}
	}
}
