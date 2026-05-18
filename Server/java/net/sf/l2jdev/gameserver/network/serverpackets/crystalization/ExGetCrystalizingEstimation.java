package net.sf.l2jdev.gameserver.network.serverpackets.crystalization;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.holders.ItemChanceHolder;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExGetCrystalizingEstimation extends ServerPacket
{
	private final List<ItemChanceHolder> _items;

	public ExGetCrystalizingEstimation(List<ItemChanceHolder> items)
	{
		this._items = items;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_CRYSTALITEM_INFO.writeId(this, buffer);
		buffer.writeInt(this._items.size());

		for (ItemChanceHolder holder : this._items)
		{
			buffer.writeInt(holder.getId());
			buffer.writeLong(holder.getCount());
			buffer.writeInt((int) (holder.getChance() * 1000.0));
		}
	}
}
