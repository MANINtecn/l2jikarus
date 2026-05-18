package net.sf.l2jdev.gameserver.network.serverpackets.primeshop;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.enums.ExBrProductReplyType;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExBRBuyProduct extends ServerPacket
{
	private final int _reply;

	public ExBRBuyProduct(ExBrProductReplyType type)
	{
		this._reply = type.getId();
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_BUY_PRODUCT_ACK.writeId(this, buffer);
		buffer.writeInt(this._reply);
	}
}
