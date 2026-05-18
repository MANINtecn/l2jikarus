package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExSearchOrc extends ServerPacket
{
	public static final ExSearchOrc STATIC_PACKET = new ExSearchOrc();

	private ExSearchOrc()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_ORC_MOVE.writeId(this, buffer);
	}
}
