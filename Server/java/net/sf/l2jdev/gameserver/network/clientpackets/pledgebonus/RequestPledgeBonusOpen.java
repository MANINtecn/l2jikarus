package net.sf.l2jdev.gameserver.network.clientpackets.pledgebonus;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgebonus.ExPledgeBonusOpen;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgedonation.ExPledgeDonationInfo;

public class RequestPledgeBonusOpen extends ClientPacket
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
			player.sendPacket(new ExPledgeBonusOpen(player));
			long joinedTime = player.getClanJoinExpiryTime() - PlayerConfig.ALT_CLAN_JOIN_MINS * 60000;
			player.sendPacket(new ExPledgeDonationInfo(player.getClanDonationPoints(), joinedTime + 86400000L < System.currentTimeMillis()));
		}
	}
}
