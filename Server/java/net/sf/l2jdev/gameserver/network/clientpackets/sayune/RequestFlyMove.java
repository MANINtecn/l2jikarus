package net.sf.l2jdev.gameserver.network.clientpackets.sayune;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.request.SayuneRequest;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;

public class RequestFlyMove extends ClientPacket
{
	private int _locationId;

	@Override
	protected void readImpl()
	{
		this._locationId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			SayuneRequest request = player.getRequest(SayuneRequest.class);
			if (request != null)
			{
				request.move(player, this._locationId);
			}
		}
	}
}
