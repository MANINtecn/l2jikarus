package net.sf.l2jdev.gameserver.network.serverpackets.pet;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.AbstractItemPacket;

public class PetItemList extends AbstractItemPacket
{
	private final Collection<Item> _items;

	public PetItemList(Collection<Item> items)
	{
		this._items = items;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PET_ITEMLIST.writeId(this, buffer);
		buffer.writeShort(this._items.size());

		for (Item item : this._items)
		{
			this.writeItem(item, buffer);
		}
	}
}
