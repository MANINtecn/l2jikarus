package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeCount;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestWithdrawalPledge extends ClientPacket
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
			else if (player.isClanLeader())
			{
				player.sendPacket(SystemMessageId.A_CLAN_LEADER_CANNOT_WITHDRAW_FROM_THEIR_OWN_CLAN);
			}
			else if (player.isInCombat())
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_LEAVE_A_CLAN_WHILE_ENGAGED_IN_COMBAT);
			}
			else
			{
				Clan clan = player.getClan();
				clan.removeClanMember(player.getObjectId(), System.currentTimeMillis() + PlayerConfig.ALT_CLAN_JOIN_MINS * 60000);
				SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_LEFT_THE_CLAN);
				sm.addString(player.getName());
				clan.broadcastToOnlineMembers(sm);
				clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(player.getName()));
				clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
				player.sendPacket(SystemMessageId.YOU_HAVE_LEFT_THE_CLAN);
				player.sendPacket(SystemMessageId.YOU_CANNOT_JOIN_ANOTHER_CLAN_FOR_24_H_AFTER_LEAVING_THE_PREVIOUS_ONE);
			}
		}
	}
}
