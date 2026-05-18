package net.sf.l2jdev.gameserver.network.clientpackets;

import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoomType;

public class RequestExOustFromMpccRoom extends ClientPacket
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
			MatchingRoom room = player.getMatchingRoom();
			if (room != null && room.getLeader() == player && room.getRoomType() == MatchingRoomType.COMMAND_CHANNEL)
			{
				Player target = World.getInstance().getPlayer(this._objectId);
				if (target != null)
				{
					room.deleteMember(target, true);
				}
			}
		}
	}
}
