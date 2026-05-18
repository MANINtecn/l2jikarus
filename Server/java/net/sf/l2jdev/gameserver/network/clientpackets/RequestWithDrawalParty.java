package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.groups.PartyMessageType;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;

public class RequestWithDrawalParty extends ClientPacket
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
			Party party = player.getParty();
			if (party != null)
			{
				party.removePartyMember(player, PartyMessageType.LEFT);
				MatchingRoom room = player.getMatchingRoom();
				if (room != null)
				{
					room.deleteMember(player, false);
				}
			}
		}
	}
}
