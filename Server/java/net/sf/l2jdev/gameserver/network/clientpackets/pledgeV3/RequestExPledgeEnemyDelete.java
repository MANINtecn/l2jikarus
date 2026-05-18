package net.sf.l2jdev.gameserver.network.clientpackets.pledgeV3;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.clientpackets.ClientPacket;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3.ExPledgeEnemyInfoList;
import net.sf.l2jdev.gameserver.taskmanagers.AttackStanceTaskManager;

public class RequestExPledgeEnemyDelete extends ClientPacket
{
	private int _clanId;

	@Override
	protected void readImpl()
	{
		this._clanId = this.readInt();
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
				Clan enemyClan = ClanTable.getInstance().getClan(this._clanId);
				if (enemyClan == null)
				{
					player.sendPacket(SystemMessageId.THE_CLAN_IS_NOT_FOUND_2);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (!playerClan.isAtWarWith(enemyClan.getId()))
				{
					player.sendPacket(SystemMessageId.ENTER_THE_NAME_OF_THE_CLAN_YOU_WISH_TO_END_THE_WAR_WITH);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (!player.hasAccess(ClanAccess.WAR_DECLARATION))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
				}
				else
				{
					for (ClanMember member : playerClan.getMembers())
					{
						if (member != null && member.getPlayer() != null && AttackStanceTaskManager.getInstance().hasAttackStanceTask(member.getPlayer()))
						{
							player.sendPacket(SystemMessageId.THE_CLAN_WAR_CANNOT_BE_STOPPED_BECAUSE_SOMEONE_FROM_YOUR_CLAN_IS_STILL_ENGAGED_IN_BATTLE);
							return;
						}
					}

					playerClan.takeReputationScore(500);
					ClanTable.getInstance().deleteClanWars(playerClan.getId(), enemyClan.getId());
					this.broadcastClanInfo(playerClan, enemyClan);
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
