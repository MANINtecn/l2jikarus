package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.NewHennaComposeProbList;

public class RequestNewHennaComposeProbList extends ClientPacket
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
			player.sendPacket(new NewHennaComposeProbList(player));
		}
	}
}
