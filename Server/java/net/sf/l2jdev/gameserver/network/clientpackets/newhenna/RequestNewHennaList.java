package net.sf.l2jdev.gameserver.network.clientpackets.newhenna;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.newhenna.NewHennaList;

public class RequestNewHennaList extends ClientPacket
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
			player.sendPacket(new NewHennaList(player, 0));
		}
	}
}
