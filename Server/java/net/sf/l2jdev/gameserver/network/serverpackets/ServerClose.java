package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ServerClose extends ServerPacket
{
	public static final ServerClose STATIC_PACKET = new ServerClose();

	private ServerClose()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SERVER_CLOSE.writeId(this, buffer);
	}
}
