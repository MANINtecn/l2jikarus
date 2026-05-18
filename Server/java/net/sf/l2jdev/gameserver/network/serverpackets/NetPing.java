package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class NetPing extends ServerPacket
{
	private final int _time;

	public NetPing(int time)
	{
		this._time = time;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.NET_PING.writeId(this, buffer);
		buffer.writeInt(this._time);
	}
}
