package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class AnswerJoinPartyRoom extends ClientPacket
{
	private boolean _answer;

	@Override
	protected void readImpl()
	{
		this._answer = this.readInt() == 1;
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player partner = player.getActiveRequester();
			if (partner == null)
			{
				player.sendPacket(SystemMessageId.THAT_PLAYER_IS_NOT_ONLINE);
				player.setActiveRequester(null);
			}
			else
			{
				if (this._answer && !partner.isRequestExpired())
				{
					MatchingRoom room = partner.getMatchingRoom();
					if (room == null)
					{
						return;
					}

					room.addMember(player);
				}
				else
				{
					partner.sendPacket(SystemMessageId.THE_RECIPIENT_OF_YOUR_INVITATION_DID_NOT_ACCEPT_THE_PARTY_MATCHING_INVITATION);
				}

				player.setActiveRequester(null);
				partner.onTransactionResponse();
			}
		}
	}
}
