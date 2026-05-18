package net.sf.l2jdev.gameserver.network.clientpackets.crossevent;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventData;
import net.sf.l2jdev.gameserver.network.serverpackets.crossevent.ExCrossEventInfo;

public class RequestCrossEventInfo extends ClientPacket
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
			player.sendPacket(new ExCrossEventData());
			player.sendPacket(new ExCrossEventInfo(player));
		}
	}
}
