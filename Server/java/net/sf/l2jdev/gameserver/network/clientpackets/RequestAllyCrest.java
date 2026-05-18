package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.AllyCrest;

public class RequestAllyCrest extends ClientPacket
{
	private int _crestId;
	private int _clanId;

	@Override
	protected void readImpl()
	{
		this.readInt();
		this._crestId = this.readInt();
		this.readInt();
		this._clanId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new AllyCrest(this._crestId, this._clanId));
		}
	}
}
