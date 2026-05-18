package net.sf.l2jdev.gameserver.network.serverpackets;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.clan.Clan;
import net.sf.l2jdev.gameserver.model.item.instance.Item;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class GMViewWarehouseWithdrawList extends AbstractItemPacket
{
	private final int _sendType;
	private final Collection<Item> _items;
	private final String _playerName;
	private final long _money;

	public GMViewWarehouseWithdrawList(int sendType, Player player)
	{
		this._sendType = sendType;
		this._items = player.getWarehouse().getItems();
		this._playerName = player.getName();
		this._money = player.getWarehouse().getAdena();
	}

	public GMViewWarehouseWithdrawList(int sendType, Clan clan)
	{
		this._sendType = sendType;
		this._playerName = clan.getLeaderName();
		this._items = clan.getWarehouse().getItems();
		this._money = clan.getWarehouse().getAdena();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.GM_VIEW_WAREHOUSE_WITHDRAW_LIST.writeId(this, buffer);
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
			buffer.writeString(this._playerName);
			buffer.writeLong(this._money);
			buffer.writeInt(this._items.size());
		}
	}
}
