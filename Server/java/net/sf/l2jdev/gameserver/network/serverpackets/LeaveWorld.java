package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class LeaveWorld extends ServerPacket
{
	public static final LeaveWorld STATIC_PACKET = new LeaveWorld();

	private LeaveWorld()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.LOGOUT_OK.writeId(this, buffer);
	}
}
