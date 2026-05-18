package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowBaseAttributeCancelWindow extends ServerPacket
{
	private final List<Item> _items = new ArrayList<>();
	private long _price;

	public ExShowBaseAttributeCancelWindow(Player player)
	{
		for (Item item : player.getInventory().getItems())
		{
			if (item.hasAttributes())
			{
				this._items.add(item);
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_BASE_ATTRIBUTE_CANCEL_WINDOW.writeId(this, buffer);
		buffer.writeInt(this._items.size());

		for (Item item : this._items)
		{
			buffer.writeInt(item.getObjectId());
			buffer.writeLong(this.getPrice(item));
		}
	}

	private long getPrice(Item item)
	{
		switch (item.getTemplate().getCrystalType())
		{
			case S:
				if (item.isWeapon())
				{
					this._price = 50000L;
				}
				else
				{
					this._price = 40000L;
				}
				break;
			case S80:
				if (item.isWeapon())
				{
					this._price = 100000L;
				}
				else
				{
					this._price = 80000L;
				}
				break;
			case S84:
				if (item.isWeapon())
				{
					this._price = 200000L;
				}
				else
				{
					this._price = 160000L;
				}
		}

		return this._price;
	}
}
