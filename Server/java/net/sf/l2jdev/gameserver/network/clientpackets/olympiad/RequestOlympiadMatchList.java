package net.sf.l2jdev.gameserver.network.clientpackets.olympiad;

import net.sf.l2jdev.gameserver.handler.BypassHandler;
import net.sf.l2jdev.gameserver.handler.IBypassHandler;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestOlympiadMatchList extends ClientPacket
{
 

	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.inObserverMode())
		{
			IBypassHandler handler = BypassHandler.getInstance().getHandler("arenalist");
			if (handler != null)
			{
				handler.onCommand("arenalist", player, null);
			}
		}
	}
}
