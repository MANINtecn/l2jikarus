package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.CastleManager;
import net.sf.l2jdev.gameserver.managers.ClanEntryManager;
import net.sf.l2jdev.gameserver.managers.FortManager;
import net.sf.l2jdev.gameserver.model.World;
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

public class RequestPledgeWaitingUserAccept extends ClientPacket
{
	private boolean _acceptRequest;
	private int _playerId;

	@Override
	protected void readImpl()
	{
		this._acceptRequest = this.readInt() == 1;
		this._playerId = this.readInt();
		this.readInt();
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
				int clanId = clan.getId();
				if (this._acceptRequest)
				{
					Player target = World.getInstance().getPlayer(this._playerId);
					if (target != null)
					{
						long currentTime = System.currentTimeMillis();
						if (target.getClan() == null && target.getClanJoinExpiryTime() < currentTime)
						{
							target.sendPacket(new JoinPledge(clan.getId()));
							target.setPowerGrade(5);
							clan.addClanMember(target);
							target.setClanPrivileges(target.getClan().getRankPrivs(target.getPowerGrade()));
							target.sendPacket(SystemMessageId.ENTERED_THE_CLAN);
							SystemMessage sm = new SystemMessage(SystemMessageId.S1_HAS_JOINED_THE_CLAN);
							sm.addString(target.getName());
							clan.broadcastToOnlineMembers(sm);
							if (clan.getCastleId() > 0)
							{
								Castle castle = CastleManager.getInstance().getCastleByOwner(clan);
								if (castle != null)
								{
									castle.giveResidentialSkills(target);
								}
							}

							if (clan.getFortId() > 0)
							{
								Fort fort = FortManager.getInstance().getFortByOwner(clan);
								if (fort != null)
								{
									fort.giveResidentialSkills(target);
								}
							}

							target.sendSkillList();
							clan.broadcastToOtherOnlineMembers(new PledgeShowMemberListAdd(target), target);
							clan.broadcastToOnlineMembers(new PledgeShowInfoUpdate(clan));
							clan.broadcastToOnlineMembers(new ExPledgeCount(clan));
							PledgeShowMemberListAll.sendAllTo(target);
							target.setClanJoinExpiryTime(0L);
							player.setClanJoinTime(currentTime);
							target.broadcastUserInfo();
							ClanEntryManager.getInstance().removePlayerApplication(clanId, this._playerId);
						}
						else if (target.getClanJoinExpiryTime() > currentTime)
						{
							SystemMessage smx = new SystemMessage(SystemMessageId.C1_WILL_BE_ABLE_TO_JOIN_YOUR_CLAN_IN_24_H_AFTER_LEAVING_THE_PREVIOUS_ONE);
							smx.addString(target.getName());
							player.sendPacket(smx);
						}
					}
				}
				else
				{
					ClanEntryManager.getInstance().removePlayerApplication(clanId, this._playerId);
				}
			}
		}
	}
}
