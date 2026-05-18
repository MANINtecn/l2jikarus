package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Set;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.sql.CharInfoTable;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class BlockListPacket extends ServerPacket
{
	private final Set<Integer> _playerIds;

	public BlockListPacket(Set<Integer> playerIds)
	{
		this._playerIds = playerIds;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.BLOCK_PACKET_LIST.writeId(this, buffer);
		buffer.writeInt(this._playerIds.size());

		for (int playerId : this._playerIds)
		{
			buffer.writeString(CharInfoTable.getInstance().getNameById(playerId));
			buffer.writeString("");
		}
	}
}
