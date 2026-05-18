package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SendTradeRequest extends ServerPacket
{
	private final int _senderId;

	public SendTradeRequest(int senderId)
	{
		this._senderId = senderId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TRADE_REQUEST.writeId(this, buffer);
		buffer.writeInt(this._senderId);
	}
}
