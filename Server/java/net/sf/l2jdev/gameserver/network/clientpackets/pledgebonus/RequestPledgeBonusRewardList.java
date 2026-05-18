package net.sf.l2jdev.gameserver.network.clientpackets.pledgebonus;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgebonus.ExPledgeBonusList;

public class RequestPledgeBonusRewardList extends ClientPacket
{
	@Override
	protected void readImpl()
	{
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null && player.getClan() != null)
		{
			player.sendPacket(new ExPledgeBonusList());
		}
	}
}
