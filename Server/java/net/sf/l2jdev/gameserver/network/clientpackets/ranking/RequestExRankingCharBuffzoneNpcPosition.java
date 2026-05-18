package net.sf.l2jdev.gameserver.network.clientpackets.ranking;

import net.sf.l2jdev.gameserver.managers.RankManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ranking.ExRankingBuffZoneNpcInfo;
import net.sf.l2jdev.gameserver.network.serverpackets.ranking.ExRankingBuffZoneNpcPosition;

public class RequestExRankingCharBuffzoneNpcPosition extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			int ranker = RankManager.getInstance().getPlayerGlobalRank(player);
			if (ranker == 1)
			{
				player.sendPacket(new ExRankingBuffZoneNpcInfo());
			}

			player.sendPacket(new ExRankingBuffZoneNpcPosition());
		}
	}
}
