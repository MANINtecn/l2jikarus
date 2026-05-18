package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeCount;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListDelete;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestOustPledgeMember extends ClientPacket
{
	private String _target;

	@Override
	protected void readImpl()
	{
		this._target = this.readString();
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
			else if (!player.hasAccess(ClanAccess.REMOVE_MEMBER))
			{
				player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
			}
			else if (player.getName().equalsIgnoreCase(this._target))
			{
				player.sendPacket(SystemMessageId.YOU_CANNOT_DISMISS_YOURSELF);
			}
			else
			{
				Clan clan = player.getClan();
				ClanMember member = clan.getClanMember(this._target);
				if (member == null)
				{
					PacketLogger.warning("Target (" + this._target + ") is not member of the clan");
				}
				else if (member.isOnline() && member.getPlayer().isInCombat())
				{
					player.sendPacket(SystemMessageId.A_CLAN_MEMBER_MAY_NOT_BE_DISMISSED_DURING_COMBAT);
				}
				else
				{
					clan.removeClanMember(member.getObjectId(), System.currentTimeMillis() + PlayerConfig.ALT_CLAN_JOIN_MINS * 60000);
					clan.setCharPenaltyExpiryTime(System.currentTimeMillis() + PlayerConfig.ALT_CLAN_JOIN_MINS * 86400000);
					clan.updateClanInDB();
					SystemMessage sm = new SystemMessage(SystemMessageId.S1_IS_DISMISSED_FROM_THE_CLAN);
					sm.addString(member.getName());
					clan.broadcastToOnlineMembers(sm);
					player.sendPacket(SystemMessageId.THE_CLAN_MEMBER_IS_DISMISSED);
					player.sendPacket(SystemMessageId.YOU_CANNOT_ACCEPT_A_NEW_CLAN_MEMBER_FOR_24_H_AFTER_DISMISSING_SOMEONE);
					clan.broadcastToOnlineMembers(new PledgeShowMemberListDelete(this._target));
					clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
					if (member.isOnline())
					{
						Player target = member.getPlayer();
						target.sendPacket(SystemMessageId.YOU_ARE_DISMISSED_FROM_A_CLAN_YOU_CANNOT_JOIN_ANOTHER_FOR_24_H);
					}
				}
			}
		}
	}
}
