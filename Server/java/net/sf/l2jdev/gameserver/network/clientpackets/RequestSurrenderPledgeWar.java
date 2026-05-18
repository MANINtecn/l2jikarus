package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanAccess;
import net.sf.l2jdev.gameserver.model.clan.ClanMember;
import net.sf.l2jdev.gameserver.model.clan.ClanWar;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanWarState;
import net.sf.l2jdev.gameserver.network.SystemMessageId;
import net.sf.l2jdev.gameserver.network.serverpackets.ActionFailed;
import net.sf.l2jdev.gameserver.network.serverpackets.SystemMessage;

public class RequestSurrenderPledgeWar extends ClientPacket
{
	private String _pledgeName;

	@Override
	protected void readImpl()
	{
		this._pledgeName = this.readString();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Clan myClan = player.getClan();
			if (myClan != null)
			{
				for (ClanMember member : myClan.getMembers())
				{
					if (member != null && member.isOnline() && member.getPlayer().isInCombat())
					{
						player.sendPacket(SystemMessageId.THE_CLAN_WAR_CANNOT_BE_STOPPED_BECAUSE_SOMEONE_FROM_YOUR_CLAN_IS_STILL_ENGAGED_IN_BATTLE);
						player.sendPacket(ActionFailed.STATIC_PACKET);
						return;
					}
				}

				Clan targetClan = ClanTable.getInstance().getClanByName(this._pledgeName);
				if (targetClan == null)
				{
					player.sendMessage("No such clan.");
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else if (!player.hasAccess(ClanAccess.WAR_DECLARATION))
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_AUTHORIZED_TO_DO_THAT);
					player.sendPacket(ActionFailed.STATIC_PACKET);
				}
				else
				{
					ClanWar clanWar = myClan.getWarWith(targetClan.getId());
					if (clanWar == null)
					{
						SystemMessage sm = new SystemMessage(SystemMessageId.YOU_HAVE_NOT_DECLARED_A_CLAN_WAR_AGAINST_THE_CLAN_S1);
						sm.addString(targetClan.getName());
						player.sendPacket(sm);
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else if (clanWar.getState() == ClanWarState.BLOOD_DECLARATION)
					{
						player.sendPacket(SystemMessageId.YOU_CANNOT_DECLARE_DEFEAT_AS_IT_HAS_NOT_BEEN_7_DAYS_SINCE_STARTING_A_CLAN_WAR_WITH_CLAN_S1);
						player.sendPacket(ActionFailed.STATIC_PACKET);
					}
					else
					{
						clanWar.cancel(player, myClan);
					}
				}
			}
		}
	}
}
