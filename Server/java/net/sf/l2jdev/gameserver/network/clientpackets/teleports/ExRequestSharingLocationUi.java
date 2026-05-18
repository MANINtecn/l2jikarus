package net.sf.l2jdev.gameserver.network.clientpackets.teleports;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.teleports.ExShowSharingLocationUi;

public class ExRequestSharingLocationUi extends ClientPacket
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
			player.sendPacket(new ExShowSharingLocationUi());
		}
	}
}
