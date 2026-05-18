package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanEntryStatus;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeRecruitApplyInfo;

public class RequestPledgeRecruitApplyInfo extends ClientPacket
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
			ClanEntryStatus status;
			if (clan != null && player.isClanLeader() && ClanEntryManager.getInstance().isClanRegistred(player.getClanId()))
			{
				status = ClanEntryStatus.ORDERED;
			}
			else if (clan == null && ClanEntryManager.getInstance().isPlayerRegistred(player.getObjectId()))
			{
				status = ClanEntryStatus.WAITING;
			}
			else
			{
				status = ClanEntryStatus.DEFAULT;
			}

			player.sendPacket(new ExPledgeRecruitApplyInfo(status));
		}
	}
}
