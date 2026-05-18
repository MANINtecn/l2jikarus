package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class TradeOtherDone extends ServerPacket
{
	public static final TradeOtherDone STATIC_PACKET = new TradeOtherDone();

	private TradeOtherDone()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.TRADE_PRESS_OTHER_OK.writeId(this, buffer);
	}
}
