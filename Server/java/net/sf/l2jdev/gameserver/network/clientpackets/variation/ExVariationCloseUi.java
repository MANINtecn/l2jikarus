package net.sf.l2jdev.gameserver.network.clientpackets.variation;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.VariationRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class ExVariationCloseUi extends ClientPacket
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
			if (player.getRequest(VariationRequest.class) != null)
			{
				player.removeRequest(VariationRequest.class);
			}
		}
	}
}
