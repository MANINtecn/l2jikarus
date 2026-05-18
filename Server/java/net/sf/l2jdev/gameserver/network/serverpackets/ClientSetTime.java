package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.taskmanagers.GameTimeTaskManager;

public class ClientSetTime extends ServerPacket
{
	public static final ClientSetTime STATIC_PACKET = new ClientSetTime();

	private ClientSetTime()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.CLIENT_SET_TIME.writeId(this, buffer);
		buffer.writeInt(GameTimeTaskManager.getInstance().getGameTime());
		buffer.writeInt(6);
	}
}
