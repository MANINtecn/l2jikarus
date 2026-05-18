package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.model.TradeItem;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TradeOtherAdd extends AbstractItemPacket
{
	private final int _sendType;
	private final TradeItem _item;

	public TradeOtherAdd(int sendType, TradeItem item)
	{
		this._sendType = sendType;
		this._item = item;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TRADE_OTHER_ADD.writeId(this, buffer);
		buffer.writeByte(this._sendType);
		if (this._sendType == 2)
		{
			buffer.writeInt(1);
		}

		buffer.writeInt(1);
		this.writeItem(this._item, buffer);
	}
}
