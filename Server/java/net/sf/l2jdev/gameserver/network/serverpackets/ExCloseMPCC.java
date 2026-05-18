package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCloseMPCC extends ServerPacket
{
	public static final ExCloseMPCC STATIC_PACKET = new ExCloseMPCC();

	private ExCloseMPCC()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CLOSE_MPCC.writeId(this, buffer);
	}
}
