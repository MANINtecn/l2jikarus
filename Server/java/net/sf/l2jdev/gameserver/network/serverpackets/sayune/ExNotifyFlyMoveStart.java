package net.sf.l2jdev.gameserver.network.serverpackets.sayune;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExNotifyFlyMoveStart extends ServerPacket
{
	public static final ExNotifyFlyMoveStart STATIC_PACKET = new ExNotifyFlyMoveStart();

	private ExNotifyFlyMoveStart()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_NOTIFY_FLY_MOVE_START.writeId(this, buffer);
	}
}
