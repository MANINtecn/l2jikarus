package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExRestartClient extends ServerPacket
{
	public static final ExRestartClient STATIC_PACKET = new ExRestartClient();

	private ExRestartClient()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RESTART_CLIENT.writeId(this, buffer);
	}
}
