package net.sf.l2jdev.gameserver.network.serverpackets.commission;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class ExResponseCommissionItemList extends AbstractItemPacket
{
	private final int _sendType;
	private final Collection<Item> _items;

	public ExResponseCommissionItemList(int sendType, Collection<Item> items)
	{
		this._sendType = sendType;
		this._items = items;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESPONSE_COMMISSION_ITEM_LIST.writeId(this, buffer);
		buffer.writeByte(this._sendType);
		if (this._sendType == 2)
		{
			buffer.writeInt(this._items.size());
			buffer.writeInt(this._items.size());

			for (Item item : this._items)
			{
				this.writeItem(item, buffer);
			}
		}
		else
		{
			buffer.writeInt(0);
			buffer.writeInt(0);
		}
	}
}
