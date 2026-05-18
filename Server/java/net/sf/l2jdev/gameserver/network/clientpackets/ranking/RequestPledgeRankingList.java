package net.sf.l2jdev.gameserver.network.clientpackets.ranking;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ranking.ExPledgeRankingList;

public class RequestPledgeRankingList extends ClientPacket
{
	private int _category;

	@Override
	protected void readImpl()
	{
		this._category = this.readByte();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			player.sendPacket(new ExPledgeRankingList(player, this._category));
		}
	}
}
