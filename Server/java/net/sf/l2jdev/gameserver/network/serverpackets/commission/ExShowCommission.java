package net.sf.l2jdev.gameserver.network.serverpackets.commission;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExShowCommission extends ServerPacket
{
	public static final ExShowCommission STATIC_PACKET = new ExShowCommission();

	private ExShowCommission()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_COMMISSION.writeId(this, buffer);
		buffer.writeInt(1);
	}
}
