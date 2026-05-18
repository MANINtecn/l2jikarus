package net.sf.l2jdev.gameserver.network.clientpackets.primeshop;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.primeshop.ExBRGamePoint;

public class RequestBRGamePoint extends ClientPacket
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
			player.sendPacket(new ExBRGamePoint(player));
		}
	}
}
