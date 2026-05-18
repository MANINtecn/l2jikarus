package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.instance.Pet;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class GMViewItemList extends AbstractItemPacket
{
	private final int _sendType;
	private final List<Item> _items = new ArrayList<>();
	private final int _limit;
	private final String _playerName;

	public GMViewItemList(int sendType, Player player)
	{
		this._sendType = sendType;
		this._playerName = player.getName();
		this._limit = player.getInventoryLimit();

		for (Item item : player.getInventory().getItems())
		{
			this._items.add(item);
		}
	}

	public GMViewItemList(int sendType, Pet cha)
	{
		this._sendType = sendType;
		this._playerName = cha.getName();
		this._limit = cha.getInventoryLimit();

		for (Item item : cha.getInventory().getItems())
		{
			this._items.add(item);
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.GM_VIEW_ITEMLIST.writeId(this, buffer);
		buffer.writeByte(this._sendType);
		if (this._sendType == 2)
		{
			buffer.writeInt(this._items.size());
		}
		else
		{
			buffer.writeString(this._playerName);
			buffer.writeInt(this._limit);
		}

		buffer.writeInt(this._items.size());

		for (Item item : this._items)
		{
			this.writeItem(item, buffer);
		}
	}
}
