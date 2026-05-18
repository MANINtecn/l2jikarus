package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestAnswerJoinAlly extends ClientPacket
{
	private int _response;

	@Override
	protected void readImpl()
	{
		this._response = this.readInt();
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
				if (this._response == 0)
				{
					player.sendPacket(SystemMessageId.NO_RESPONSE_YOUR_ENTRANCE_TO_THE_ALLIANCE_HAS_BEEN_CANCELLED);
					requestor.sendPacket(SystemMessageId.NO_RESPONSE_THE_INVITATION_TO_JOIN_THE_ALLIANCE_IS_CANCELLED);
				}
				else
				{
					if (!(requestor.getRequest().getRequestPacket() instanceof RequestJoinAlly))
					{
						return;
					}

					Clan requestorClan = requestor.getClan();
					if (requestorClan.checkAllyJoinCondition(requestor, player))
					{
						requestor.sendPacket(SystemMessageId.THAT_PERSON_HAS_BEEN_SUCCESSFULLY_ADDED_TO_YOUR_FRIEND_LIST);
						player.sendPacket(SystemMessageId.YOU_HAVE_ACCEPTED_THE_ALLIANCE);
						Clan clan = player.getClan();
						clan.setAllyId(requestorClan.getAllyId());
						clan.setAllyName(requestorClan.getAllyName());
						clan.setAllyPenaltyExpiryTime(0L, 0);
						clan.changeAllyCrest(requestorClan.getAllyCrestId(), true);
						clan.updateClanInDB();
						clan.broadcastClanStatus();
						requestorClan.broadcastClanStatus();
					}
				}

				player.getRequest().onRequestResponse();
			}
		}
	}
}
