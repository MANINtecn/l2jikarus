package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.PacketLogger;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class WareHouseWithdrawalList extends AbstractItemPacket
{
	public static final int PRIVATE = 1;
	public static final int CLAN = 2;
	public static final int CASTLE = 3;
	public static final int FREIGHT = 1;
	private final int _sendType;
	private Player _player;
	private long _playerAdena;
	private final int _invSize;
	private Collection<Item> _items;
	private final List<Integer> _itemsStackable = new ArrayList<>();
	private int _whType;

	public WareHouseWithdrawalList(int sendType, Player player, int type)
	{
		this._sendType = sendType;
		this._player = player;
		this._whType = type;
		this._playerAdena = this._player.getAdena();
		this._invSize = player.getInventory().getSize();
		if (this._player.getActiveWarehouse() == null)
		{
			PacketLogger.warning("error while sending withdraw request to: " + this._player.getName());
		}
		else
		{
			this._items = this._player.getActiveWarehouse().getItems();

			for (Item item : this._items)
			{
				if (item.isStackable())
				{
					this._itemsStackable.add(item.getDisplayId());
				}
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.WAREHOUSE_WITHDRAW_LIST.writeId(this, buffer);
		buffer.writeByte(this._sendType);
		if (this._sendType == 2)
		{
			buffer.writeShort(0);
			buffer.writeInt(this._invSize);
			buffer.writeInt(this._items.size());

			for (Item item : this._items)
			{
				this.writeItem(item, buffer);
				buffer.writeInt(item.getObjectId());
				buffer.writeInt(0);
				buffer.writeInt(0);
			}
		}
		else
		{
			buffer.writeShort(this._whType);
			buffer.writeLong(this._playerAdena);
			buffer.writeInt(this._invSize);
			buffer.writeInt(this._items.size());
		}
	}
}
