package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.Party;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoomType;
import net.sf.l2jdev.gameserver.network.SystemMessageId;

public class RequestOustFromPartyRoom extends ClientPacket
{
	private int _objectId;

	@Override
	protected void readImpl()
	{
		this._objectId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			Player member = World.getInstance().getPlayer(this._objectId);
			if (member != null)
			{
				MatchingRoom room = player.getMatchingRoom();
				if (room != null && room.getRoomType() == MatchingRoomType.PARTY && room.getLeader() == player && player != member)
				{
					Party playerParty = player.getParty();
					Party memberParty = player.getParty();
					if (playerParty != null && memberParty != null && playerParty.getLeaderObjectId() == memberParty.getLeaderObjectId())
					{
						player.sendPacket(SystemMessageId.FAILED_TO_DISMISS_THE_PARTY_MEMBER_2);
					}
					else
					{
						room.deleteMember(member, true);
					}
				}
			}
		}
	}
}
