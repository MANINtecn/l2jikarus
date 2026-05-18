package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.TradeItem;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TradeUpdate extends AbstractItemPacket
{
	private final int _sendType;
	private final TradeItem _item;
	private final long _newCount;
	private final long _count;

	public TradeUpdate(int sendType, Player player, TradeItem item, long count)
	{
		this._sendType = sendType;
		this._count = count;
		this._item = item;
		this._newCount = player == null ? 0L : player.getInventory().getItemByObjectId(item.getObjectId()).getCount() - item.getCount();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TRADE_UPDATE.writeId(this, buffer);
		buffer.writeByte(this._sendType);
		buffer.writeInt(1);
		if (this._sendType == 2)
		{
			buffer.writeInt(1);
			buffer.writeShort(this._newCount > 0L && this._item.getItem().isStackable() ? 3 : 2);
			this.writeItem(this._item, this._count, buffer);
		}
	}
}
