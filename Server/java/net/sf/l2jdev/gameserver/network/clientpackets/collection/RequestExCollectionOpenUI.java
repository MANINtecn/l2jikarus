package net.sf.l2jdev.gameserver.network.clientpackets.collection;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.AutoPeelRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.collection.ExCollectionOpenUI;

public class RequestExCollectionOpenUI extends ClientPacket
{
	@Override
	protected void readImpl()
	{
		this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (!player.hasRequest(AutoPeelRequest.class))
			{
				player.setTarget(null);
				player.sendPacket(new ExCollectionOpenUI());
			}
		}
	}
}
