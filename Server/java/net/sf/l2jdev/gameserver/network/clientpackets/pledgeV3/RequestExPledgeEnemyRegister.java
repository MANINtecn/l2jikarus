package net.sf.l2jdev.gameserver.network.clientpackets.pledgeV3;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.clan.ClanWar;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanWarState;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3.ExPledgeEnemyInfoList;

public class RequestExPledgeEnemyRegister extends ClientPacket
{
	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		this._pledgeName = this.readSizedString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan playerClan = player.getClan();
			if (playerClan != null)
			{
				if (!player.hasAccess(ClanAccess.WAR_DECLARATION))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (playerClan.getWarCount() >= 30)
				{
					player.sendPacket(SystemMessageId.YOU_CANNOT_DECLARE_WAR_ON_MORE_THAN_30_CLANS_AT_A_TIME);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					Clan enemyClan = ClanTable.getInstance().getClanByName(this._pledgeName);
					if (enemyClan == null)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.A_CLAN_WAR_CANNOT_BE_DECLARED_AGAINST_A_CLAN_THAT_DOES_NOT_EXIST));
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (enemyClan == playerClan)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.FOOL_YOU_CANNOT_DECLARE_WAR_AGAINST_YOUR_OWN_CLAN));
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (playerClan.getAllyId() == enemyClan.getAllyId() && playerClan.getAllyId() != 0)
					{
						player.sendPacket(new SystemMessage(SystemMessageId.YOU_CANNOT_DECLARE_WAR_ON_AN_ALLIED_CLAN_2));
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (enemyClan.getDissolvingExpiryTime() > System.currentTimeMillis())
					{
						player.sendPacket(new SystemMessage(SystemMessageId.A_CLAN_WAR_CAN_NOT_BE_DECLARED_AGAINST_A_CLAN_THAT_IS_BEING_DISSOLVED));
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						ClanWar clanWar = playerClan.getWarWith(enemyClan.getId());
						if (clanWar != null)
						{
							if (clanWar.getClanWarState(playerClan) == ClanWarState.WIN)
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.YOU_CANNOT_DECLARE_WAR_AS_THE_21_DAY_PERIOD_HAS_NOT_PASSED_SINCE_THE_DEFEAT_FROM_THE_S1_CLAN);
								sm.addString(enemyClan.getName());
								player.sendPacket(sm);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}

							if (clanWar.getClanWarState(playerClan) != ClanWarState.BLOOD_DECLARATION || clanWar.getAttackerClanId() == playerClan.getId())
							{
								SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_ALREADY_BEEN_AT_WAR_WITH_THE_S1_CLAN_5_DAYS_MUST_PASS_BEFORE_YOU_CAN_DECLARE_WAR_AGAIN);
								sm.addString(enemyClan.getName());
								player.sendPacket(sm);
								player.sendPacket(ActionFailed.STATIC_PACKET);
								return;
							}

							if (clanWar.getClanWarState(playerClan) == ClanWarState.BLOOD_DECLARATION)
							{
								clanWar.mutualClanWarAccepted(enemyClan, playerClan);
								this.broadcastClanInfo(playerClan, enemyClan);
								return;
							}
						}

						ClanWar newClanWar = new ClanWar(playerClan, enemyClan);
						ClanTable.getInstance().storeClanWars(newClanWar);
						this.broadcastClanInfo(playerClan, enemyClan);
					}
				}
			}
		}
	}

	protected void broadcastClanInfo(Clan playerClan, Clan enemyClan)
	{
		for (ClanMember member : playerClan.getMembers())
		{
			if (member != null && member.isOnline())
			{
				member.getPlayer().sendPacket(new ExPledgeEnemyInfoList(playerClan));
				member.getPlayer().broadcastUserInfo();
			}
		}

		for (ClanMember memberx : enemyClan.getMembers())
		{
			if (memberx != null && memberx.isOnline())
			{
				memberx.getPlayer().sendPacket(new ExPledgeEnemyInfoList(enemyClan));
				memberx.getPlayer().broadcastUserInfo();
			}
		}
	}
}
