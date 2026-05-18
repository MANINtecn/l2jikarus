package net.sf.l2jdev.gameserver.network.clientpackets.castlewar;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.castlewar.MercenaryPledgeMemberList;

public class ExPledgeMercenaryMemberList extends ClientPacket
{
	private int _castleId;
	private int _pledgeId;

	@Override
	protected void readImpl()
	{
		this._castleId = this.readInt();
		this._pledgeId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new MercenaryPledgeMemberList(this._castleId, this._pledgeId));
		}
	}
}
