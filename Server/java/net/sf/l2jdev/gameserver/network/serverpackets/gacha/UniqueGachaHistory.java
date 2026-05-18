package net.sf.l2jdev.gameserver.network.serverpackets.gacha;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.holders.GachaItemTimeStampHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class UniqueGachaHistory extends ServerPacket
{
 
	private final List<GachaItemTimeStampHolder> _items;

	public UniqueGachaHistory(List<GachaItemTimeStampHolder> items)
	{
		this._items = items;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UNIQUE_GACHA_HISTORY.writeId(this, buffer);
		buffer.writeInt(this._items.size());

		for (GachaItemTimeStampHolder item : this._items)
		{
			buffer.writeShort(15);
			buffer.writeByte(item.getRank().getClientId());
			buffer.writeInt(item.getId());
			buffer.writeLong(item.getCount());
			buffer.writeInt(item.getTimeStampFromNow());
		}
	}
}
