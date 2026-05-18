package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExRedSky extends ServerPacket
{
	private final int _duration;

	public ExRedSky(int duration)
	{
		this._duration = duration;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_REDSKY.writeId(this, buffer);
		buffer.writeInt(this._duration);
	}
}
