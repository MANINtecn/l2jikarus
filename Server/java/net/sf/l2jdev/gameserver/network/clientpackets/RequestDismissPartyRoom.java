package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoomType;

public class RequestDismissPartyRoom extends ClientPacket
{
	private int _roomid;

	@Override
	protected void readImpl()
	{
		this._roomid = this.readInt();
		this.readInt();
	}

	@Override
	protected void runImpl()
	{
		Player player = this.getPlayer();
		if (player != null)
		{
			MatchingRoom room = player.getMatchingRoom();
			if (room != null && room.getId() == this._roomid && room.getRoomType() == MatchingRoomType.PARTY && room.getLeader() == player)
			{
				room.disbandRoom();
			}
		}
	}
}
