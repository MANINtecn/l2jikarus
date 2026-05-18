package net.sf.l2jdev.gameserver.network.clientpackets.balok;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.balok.BalrogWarShowUI;

public class ExBalrogWarShowUI extends ClientPacket
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
			player.sendPacket(new BalrogWarShowUI(player));
		}
	}
}
