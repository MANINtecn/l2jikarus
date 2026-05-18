package net.sf.l2jdev.gameserver.network.clientpackets.collection;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.collection.ExCollectionSummary;

public class RequestExCollectionSummary extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getClient().getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExCollectionSummary(player));
		}
	}
}
