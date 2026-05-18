package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.DuelManager;
import net.sf.l2jdev.gameserver.model.actor.Player;

public class RequestDuelSurrender extends ClientPacket
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
			DuelManager.getInstance().doSurrender(player);
		}
	}
}
