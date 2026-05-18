package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestPledgeSetMemberPowerGrade extends ClientPacket
{
	private String _member;
	private int _powerGrade;

	@Override
	protected void readImpl()
	{
		this._member = this.readString();
		this._powerGrade = this.readInt();
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
				if (player.hasAccess(ClanAccess.MODIFY_RANKS))
				{
					ClanMember member = clan.getClanMember(this._member);
					if (member != null)
					{
						if (member.getObjectId() != clan.getLeaderId())
						{
							if (member.getPledgeType() == -1)
							{
								player.sendPacket(SystemMessageId.THAT_PRIVILEGE_CANNOT_BE_GRANTED_TO_A_CLAN_ACADEMY_MEMBER);
							}
							else
							{
								member.setPowerGrade(this._powerGrade);
								clan.broadcastToOnlineMembers(new PledgeShowMemberListUpdate(member));
								clan.broadcastToOnlineMembers(new SystemMessage(SystemMessageId.CLAN_MEMBER_C1_S_PRIVILEGE_LEVEL_HAS_BEEN_CHANGED_TO_S2).addString(member.getName()).addInt(this._powerGrade));
								clan.broadcastClanStatus();
							}
						}
					}
				}
			}
		}
	}
}
