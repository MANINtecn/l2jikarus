package net.sf.l2jdev.gameserver.network.clientpackets.prison;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestPrisonUserDonation extends ClientPacket
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
			player.getPrisonerInfo().requestFreedomByDonation(player);
		}
	}
}
