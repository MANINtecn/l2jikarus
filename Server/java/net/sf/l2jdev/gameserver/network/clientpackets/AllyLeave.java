package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class AllyLeave extends ClientPacket
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
			if (player.getClan() == null)
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_2);
			}
			else if (!player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.ONLY_THE_CLAN_LEADER_MAY_APPLY_FOR_WITHDRAWAL_FROM_THE_ALLIANCE);
			}
			else
			{
				Clan clan = player.getClan();
				if (clan.getAllyId() == 0)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_AN_ALLIANCE);
				}
				else if (clan.getId() == clan.getAllyId())
				{
					player.sendPacket(SystemMessageId.ALLIANCE_LEADERS_CANNOT_WITHDRAW);
				}
				else
				{
					long currentTime = System.currentTimeMillis();
					clan.setAllyId(0);
					clan.setAllyName(null);
					clan.changeAllyCrest(0, true);
					clan.setAllyPenaltyExpiryTime(currentTime + PlayerConfig.ALT_ALLY_JOIN_DAYS_WHEN_LEAVED * 86400000, 1);
					clan.updateClanInDB();
					clan.broadcastClanStatus();
					player.sendPacket(SystemMessageId.YOU_HAVE_LEFT_THE_ALLIANCE);
				}
			}
		}
	}
}
