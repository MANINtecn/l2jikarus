package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.PartyMatchingHistoryTable;
import net.sf.l2jdev.gameserver.model.groups.matching.MatchingRoomHistory;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExRequestPartyMatchingHistory extends ServerPacket
{
	private final Collection<MatchingRoomHistory> _history = PartyMatchingHistoryTable.getInstance().getHistory();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PARTY_MATCHING_ROOM_HISTORY.writeId(this, buffer);
		buffer.writeInt(this._history.size());

		for (MatchingRoomHistory holder : this._history)
		{
			buffer.writeString(holder.getTitle());
			buffer.writeString(holder.getLeader());
		}
	}
}
