package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.data.sql.ClanTable;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.clan.ClanWar;
import net.sf.l2jdev.gameserver.model.clan.enums.ClanWarState;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestReplyStartPledgeWar extends ClientPacket
{
	private int _answer;

	@Override
	protected void readImpl()
	{
		this.readString();
		this._answer = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player requestor = player.getActiveRequester();
			if (requestor != null)
			{
				if (this._answer == 1)
				{
					Clan attacked = player.getClan();
					Clan attacker = requestor.getClan();
					if (attacked != null && attacker != null)
					{
						ClanWar clanWar = attacker.getWarWith(attacked.getId());
						if (clanWar.getState() == ClanWarState.BLOOD_DECLARATION)
						{
							clanWar.mutualClanWarAccepted(attacker, attacked);
							ClanTable.getInstance().storeClanWars(clanWar);
						}
					}
				}
				else
				{
					requestor.sendPacket(SystemMessageId.THE_S1_CLAN_DID_NOT_RESPOND_WAR_PROCLAMATION_HAS_BEEN_REFUSED_2);
				}

				player.setActiveRequester(null);
				requestor.onTransactionResponse();
			}
		}
	}
}
