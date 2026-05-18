package net.sf.l2jdev.gameserver.network.clientpackets.olympiad;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.olympiad.OlympiadMode;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.olympiad.ExOlympiadMode;

public class RequestOlympiadObserverEnd extends ClientPacket
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
			if (player.inObserverMode())
			{
				player.leaveOlympiadObserverMode();
			}
			else
			{
				player.sendPacket(new ExOlympiadMode(OlympiadMode.NONE));
			}
		}
	}
}
