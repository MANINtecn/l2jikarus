package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.PacketLogger;

public class RequestGetBossRecord extends ClientPacket
{
	private int _bossId;

	@Override
	protected void readImpl()
	{
		this._bossId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			PacketLogger.warning(player + " (boss ID: " + this._bossId + ") used unsuded packet " + RequestGetBossRecord.class.getSimpleName());
		}
	}
}
