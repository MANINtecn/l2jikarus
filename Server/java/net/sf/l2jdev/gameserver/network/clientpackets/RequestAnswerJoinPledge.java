package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.siege.Castle;
import net.sf.l2jdev.gameserver.model.siege.Fort;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ExPledgeCount;
import net.sf.l2jdev.gameserver.network.serverpackets.JoinPledge;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowInfoUpdate;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListAdd;
import net.sf.l2jdev.gameserver.network.serverpackets.PledgeShowMemberListAll;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestAnswerJoinPledge extends ClientPacket
{
	private int _answer;

	@Override
	protected void readImpl()
	{
		this._answer = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player requestor = player.getRequest().getPartner();
			if (requestor != null)
			{
				if (this._answer == 0)
				{
					SystemMessage sm = new SystemMessage(SystemMessageId.YOU_DIDN_T_RESPOND_TO_S1_S_INVITATION_JOINING_HAS_BEEN_CANCELLED);
					sm.addString(requestor.getName());
					player.sendPacket(sm);
					sm = new SystemMessage(SystemMessageId.S1_DID_NOT_RESPOND_INVITATION_TO_THE_CLAN_HAS_BEEN_CANCELLED);
					sm.addString(player.getName());
					requestor.sendPacket(sm);
				}
				else
				{
					if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge) && !(requestor.getRequest().getRequestPacket() instanceof RequestClanAskJoinByName))
					{
						return;
					}

					int pledgeType;
					if (requestor.getRequest().getRequestPacket() instanceof RequestJoinPledge)
					{
						pledgeType = ((RequestJoinPledge) requestor.getRequest().getRequestPacket()).getPledgeType();
					}
					else
					{
						pledgeType = ((RequestClanAskJoinByName) requestor.getRequest().getRequestPacket()).getPledgeType();
					}

					Clan clan = requestor.getClan();
					if (clan.checkClanJoinCondition(requestor, player, pledgeType))
					{
						if (player.getClan() != null)
						{
							return;
						}

						player.sendPacket(new JoinPledge(requestor.getClanId()));
						player.setPledgeType(pledgeType);
						if (pledgeType == -1)
						{
							player.setPowerGrade(9);
							player.setLvlJoinedAcademy(player.getLevel());
						}
						else
						{
							player.setPowerGrade(5);
						}

						clan.addClanMember(player);
						player.setClanPrivileges(player.getClan().getRankPrivs(player.getPowerGrade()));
						player.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
						SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_THE_CLAN);
						sm.addString(player.getName());
						clan.broadcastToOnlineMembers(sm);
						if (clan.getCastleId() > 0)
						{
							Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
							if (castle != null)
							{
								castle.giveResidentialSkills(player);
							}
						}

						if (clan.getFortId() > 0)
						{
							Fort fort = FortManager.getInstance().getFortByOwner(clan);
							if (fort != null)
							{
								fort.giveResidentialSkills(player);
							}
						}

						player.sendSkillList();
						clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(player), player);
						clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
						clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
						PledgeShowMemberListAll.sendAllTo(player);
						player.setClanJoinExpiryTime(0L);
						player.broadcastUserInfo();
					}
				}

				player.getRequest().onRequestResponse();
			}
		}
	}
}
