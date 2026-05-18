package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;

public class ExRequestAutoFish extends ClientPacket
{
	private boolean _start;

	@Override
	protected void readImpl()
	{
		this._start = this.readByte() != 0;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			if (this._start)
			{
				player.getFishing().startFishing();
			}
			else
			{
				player.getFishing().stopFishing();
			}
		}
	}
}
