package net.sf.l2jdev.gameserver.network.serverpackets.worldexchange;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class WorldExchangeTotalList extends ServerPacket
{
	private final Collection<Integer> _itemIds;

	public WorldExchangeTotalList(Collection<Integer> itemIds)
	{
		this._itemIds = itemIds;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_WORLD_EXCHANGE_TOTAL_LIST.writeId(this, buffer);
		buffer.writeInt(this._itemIds.size());

		for (int id : this._itemIds)
		{
			buffer.writeInt(id);
			buffer.writeLong(0L);
			buffer.writeLong(0L);
			buffer.writeLong(1L);
		}
	}
}
