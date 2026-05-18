package net.sf.l2jdev.gameserver.network.clientpackets.pledgedonation;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgedonation.ExPledgeContributionList;

public class RequestExPledgeContributionList extends ClientPacket
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
			Clan clan = player.getClan();
			if (clan != null)
			{
				player.sendPacket(new ExPledgeContributionList(clan.getContributionList()));
			}
		}
	}
}
