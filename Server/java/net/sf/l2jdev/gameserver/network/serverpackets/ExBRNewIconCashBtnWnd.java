package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExBRNewIconCashBtnWnd extends ServerPacket
{
	private final short _active;

	public ExBRNewIconCashBtnWnd(short active)
	{
		this._active = active;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BR_EXIST_NEW_PRODUCT_ACK.writeId(this, buffer);
		buffer.writeShort(this._active);
	}
}
