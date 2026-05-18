package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoomType;

public class RequestWithdrawPartyRoom extends ClientPacket
{
	private int _roomId;

	@Override
	protected void readImpl()
	{
		this._roomId = this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			MatchingRoom room = player.getMatchingRoom();
			if (room != null)
			{
				if (room.getId() == this._roomId && room.getRoomType() == MatchingRoomType.PARTY)
				{
					room.deleteMember(player, false);
				}
			}
		}
	}
}
