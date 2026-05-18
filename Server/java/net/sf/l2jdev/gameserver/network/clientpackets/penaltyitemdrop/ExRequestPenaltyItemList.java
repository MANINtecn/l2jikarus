package net.sf.l2jdev.gameserver.network.clientpackets.penaltyitemdrop;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.penaltyitemdrop.ExPenaltyItemList;

public class ExRequestPenaltyItemList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	public void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExPenaltyItemList(player));
		}
	}
}
