package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class WareHouseDepositList extends AbstractItemPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3;
	public static final int FREIGHT = 1;
	private final int _sendType;
	private final long _playerAdena;
	private final List<Item> _items = new ArrayList<>();
	private final List<Integer> _itemsStackable = new ArrayList<>();
	private final int _whType;

	public WareHouseDepositList(int sendType, Player player, int type)
	{
		this._sendType = sendType;
		this._whType = type;
		this._playerAdena = player.getAdena();
		boolean isPrivate = this._whType == 1;

		for (Item temp : player.getInventory().getAvailableItems(true, isPrivate, false))
		{
			if (temp != null && temp.isDepositable(isPrivate))
			{
				this._items.add(temp);
			}

			if (temp != null && temp.isDepositable(isPrivate) && temp.isStackable())
			{
				this._itemsStackable.add(temp.getDisplayId());
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.WAREHOUSE_DEPOSIT_LIST.writeId(this, buffer);
		buffer.writeByte(this._sendType);
		if (this._sendType == 2)
		{
			buffer.writeInt(this._whType);
			buffer.writeInt(this._items.size());

			for (Item item : this._items)
			{
				this.writeItem(item, buffer);
				buffer.writeInt(item.getObjectId());
			}
		}
		else
		{
			buffer.writeShort(this._whType);
			buffer.writeLong(this._playerAdena);
			buffer.writeInt(this._itemsStackable.size());
			buffer.writeInt(this._items.size());
		}
	}
}
