package net.sf.l2jdev.gameserver.network.clientpackets.crystalization;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestCrystallizeItemCancel extends ClientPacket
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
			if (player.isInCrystallize())
			{
				player.setInCrystallize(false);
			}
		}
	}
}
