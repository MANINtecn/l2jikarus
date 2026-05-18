package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PackageSendableList extends AbstractItemPacket
{
	private final Collection<Item> _items;
	private final int _objectId;
	private final long _adena;
	private final int _sendType;

	public PackageSendableList(int sendType, Player player, int objectId)
	{
		this._sendType = sendType;
		this._items = player.getInventory().getAvailableItems(true, true, true);
		this._objectId = objectId;
		this._adena = player.getAdena();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PACKAGE_SENDABLE_LIST.writeId(this, buffer);
		buffer.writeByte(this._sendType);
		if (this._sendType == 2)
		{
			buffer.writeInt(this._items.size());
			buffer.writeInt(this._items.size());

			for (Item item : this._items)
			{
				this.writeItem(item, buffer);
				buffer.writeInt(item.getObjectId());
			}
		}
		else
		{
			buffer.writeInt(this._objectId);
			buffer.writeLong(this._adena);
			buffer.writeInt(this._items.size());
		}
	}
}
