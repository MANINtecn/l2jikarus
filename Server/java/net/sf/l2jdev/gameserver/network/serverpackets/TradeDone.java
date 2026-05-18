package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TradeDone extends ServerPacket
{
	private final int _num;

	public TradeDone(int num)
	{
		this._num = num;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TRADE_DONE.writeId(this, buffer);
		buffer.writeInt(this._num);
	}
}
