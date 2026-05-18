package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeWaitingList;

public class RequestPledgeWaitingList extends ClientPacket
{
	private int _clanId;

	@Override
	protected void readImpl()
	{
		this._clanId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.getClanId() == this._clanId)
		{
			player.sendPacket(new ExPledgeWaitingList(this._clanId));
		}
	}
}
