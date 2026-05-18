package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.LinkedList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.MatchingRoomManager;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoom;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExListMpccWaiting extends ServerPacket
{
 
	private final int _size;
	private final List<MatchingRoom> _rooms = new LinkedList<>();

	public ExListMpccWaiting(int page, int location, int level)
	{
		List<MatchingRoom> rooms = MatchingRoomManager.getInstance().getCCMathchingRooms(location, level);
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
		ServerPackets.EX_LIST_MPCC_WAITING.writeId(this, buffer);
		buffer.writeInt(this._size);
		buffer.writeInt(this._rooms.size());

		for (MatchingRoom room : this._rooms)
		{
			buffer.writeInt(room.getId());
			buffer.writeString(room.getTitle());
			buffer.writeInt(room.getMembersCount());
			buffer.writeInt(room.getMinLevel());
			buffer.writeInt(room.getMaxLevel());
			buffer.writeInt(room.getLocation());
			buffer.writeInt(room.getMaxMembers());
			buffer.writeString(room.getLeader().getName());
		}
	}
}
