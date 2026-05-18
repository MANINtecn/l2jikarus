package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExMailArrived extends ServerPacket
{
	public static final ExMailArrived STATIC_PACKET = new ExMailArrived();

	private ExMailArrived()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_MAIL_ARRIVED.writeId(this, buffer);
	}
}
