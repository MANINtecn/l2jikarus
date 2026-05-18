package net.sf.l2jdev.gameserver.network.serverpackets.commission;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCloseCommission extends ServerPacket
{
	public static final ExCloseCommission STATIC_PACKET = new ExCloseCommission();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CLOSE_COMMISSION.writeId(this, buffer);
	}
}
