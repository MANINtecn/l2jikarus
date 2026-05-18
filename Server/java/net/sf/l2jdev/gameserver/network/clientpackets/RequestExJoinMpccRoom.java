package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.managers.MatchingRoomManager;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;

public class RequestExJoinMpccRoom extends ClientPacket
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
		if (player != null && player.getMatchingRoom() == null)
		{
			MatchingRoom room = MatchingRoomManager.getInstance().getCCMatchingRoom(this._roomId);
			if (room != null)
			{
				room.addMember(player);
			}
		}
	}
}
