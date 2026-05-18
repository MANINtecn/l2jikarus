package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExDissmissMPCCRoom extends ServerPacket
{
	public static final ExDissmissMPCCRoom STATIC_PACKET = new ExDissmissMPCCRoom();

	private ExDissmissMPCCRoom()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DISMISS_MPCC_ROOM.writeId(this, buffer);
	}
}
