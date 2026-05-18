package net.sf.l2jdev.gameserver.network.serverpackets.gacha;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class UniqueGachaInvenItemList extends ServerPacket
{
	private final int _currPage;
	private final int _maxPages;
	private final List<Item> _items;

	public UniqueGachaInvenItemList(int currPage, int maxPages, List<Item> items)
	{
		this._currPage = currPage;
		this._maxPages = maxPages;
		this._items = items;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UNIQUE_GACHA_INVEN_ITEM_LIST.writeId(this, buffer);
		buffer.writeByte(this._currPage);
		buffer.writeByte(this._maxPages);
		buffer.writeInt(this._items.size());

		for (Item item : this._items)
		{
			buffer.writeInt(item.getDisplayId());
			buffer.writeLong(item.getCount());
		}
	}
}
