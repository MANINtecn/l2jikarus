package net.sf.l2jdev.gameserver.network.serverpackets.crossevent;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCrossEventReset extends ServerPacket
{
	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CROSS_EVENT_RESET.writeId(this, buffer);
		buffer.writeByte(1);
	}
}
