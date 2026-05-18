package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.config.PlayerConfig;
import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class AllyDismiss extends ClientPacket
{
	private String _clanName;

	@Override
	protected void readImpl()
	{
		this._clanName = this.readString();
	}

	@Override
	protected void runImpl()
	{
		if (this._clanName != null)
		{
			Player player = this.getPlayer();
			if (player != null)
			{
				if (player.getClan() == null)
				{
					player.sendPacket(SystemMessageId.YOU_ARE_NOT_A_CLAN_MEMBER_2);
				}
				else
				{
					Clan leaderClan = player.getClan();
					if (leaderClan.getAllyId() == 0)
					{
						player.sendPacket(SystemMessageId.YOU_ARE_NOT_IN_AN_ALLIANCE);
					}
					else if (player.isClanLeader() && leaderClan.getId() == leaderClan.getAllyId())
					{
						Clan clan = ClanTable.getInstance().getClanByName(this._clanName);
						if (clan == null)
						{
							player.sendPacket(SystemMessageId.THE_CLAN_IS_NOT_FOUND);
						}
						else if (clan.getId() == leaderClan.getId())
						{
							player.sendPacket(SystemMessageId.ALLIANCE_LEADERS_CANNOT_WITHDRAW);
						}
						else if (clan.getAllyId() != leaderClan.getAllyId())
						{
							player.sendPacket(SystemMessageId.DIFFERENT_ALLIANCE);
						}
						else
						{
							long currentTime = System.currentTimeMillis();
							leaderClan.setAllyPenaltyExpiryTime(currentTime + PlayerConfig.ALT_ACCEPT_CLAN_DAYS_WHEN_DISMISSED * 86400000, 3);
							leaderClan.updateClanInDB();
							clan.setAllyId(0);
							clan.setAllyName(null);
							clan.changeAllyCrest(0, true);
							clan.setAllyPenaltyExpiryTime(currentTime + PlayerConfig.ALT_ALLY_JOIN_DAYS_WHEN_DISMISSED * 86400000, 2);
							clan.updateClanInDB();
							player.sendPacket(SystemMessageId.THE_CLAN_IS_DISMISSED_FROM_THE_ALLIANCE);
						}
					}
					else
					{
						player.sendPacket(SystemMessageId.ACCESS_ONLY_FOR_THE_CHANNEL_FOUNDER);
					}
				}
			}
		}
	}
}
