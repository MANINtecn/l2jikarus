package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.MatchingRoomManager;
import net.sf.l2jdev.gameserver.model.World;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.model.groups.matching.PartyMatchingRoomLevelType;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ListPartyWaiting extends ServerPacket
{
	 
	private final List<MatchingRoom> _rooms = new LinkedList<>();
	private final int _size;

	public ListPartyWaiting(PartyMatchingRoomLevelType type, int location, int page, int requestorLevel)
	{
		List<MatchingRoom> rooms = MatchingRoomManager.getInstance().getPartyMathchingRooms(location, type, requestorLevel);
		this._size = rooms.size();
		int startIndex = (page - 1) * 64;
		int chunkSize = this._size - startIndex;
		if (chunkSize > 64)
		{
			chunkSize = 64;
		}

		for (int i = startIndex; i < startIndex + chunkSize; i++)
		{
			this._rooms.add(rooms.get(i));
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.LIST_PARTY_WAITING.writeId(this, buffer);
		buffer.writeInt(this._size);
		buffer.writeInt(this._rooms.size());

		for (MatchingRoom room : this._rooms)
		{
			buffer.writeInt(room.getId());
			buffer.writeString(room.getTitle());
			buffer.writeInt(room.getLocation());
			buffer.writeInt(room.getMinLevel());
			buffer.writeInt(room.getMaxLevel());
			buffer.writeInt(room.getMaxMembers());
			buffer.writeString(room.getLeader().getName());
			buffer.writeInt(room.getMembersCount());

			for (Player member : room.getMembers())
			{
				buffer.writeInt(member.getPlayerClass().getId());
				buffer.writeString(member.getName());
			}
		}

		buffer.writeInt(World.getInstance().getPartyCount());
		buffer.writeInt(World.getInstance().getPartyMemberCount());
	}
}
