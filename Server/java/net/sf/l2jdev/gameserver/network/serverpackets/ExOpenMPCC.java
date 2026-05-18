package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExOpenMPCC extends ServerPacket
{
	public static final ExOpenMPCC STATIC_PACKET = new ExOpenMPCC();

	private ExOpenMPCC()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OPEN_MPCC.writeId(this, buffer);
	}
}
