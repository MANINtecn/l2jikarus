package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExInzoneWaiting;

public class RequestInzoneWaitingTime extends ClientPacket
{
	private boolean _hide;

	@Override
	protected void readImpl()
	{
		this._hide = this.readByte() == 0;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExInzoneWaiting(player, this._hide));
		}
	}
}
