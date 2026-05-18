package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Map;
import java.util.Map.Entry;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.managers.events.ItemDeletionInfoManager;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ItemDeletionInfo extends ServerPacket
{
	private final Map<Integer, Integer> _itemDates = ItemDeletionInfoManager.getInstance().getItemDates();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ITEM_DELETION_INFO.writeId(this, buffer);
		buffer.writeInt(this._itemDates.size());

		for (Entry<Integer, Integer> info : this._itemDates.entrySet())
		{
			buffer.writeInt(info.getKey());
			buffer.writeInt(info.getValue());
		}

		buffer.writeInt(0);
	}
}
