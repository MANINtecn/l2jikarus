package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExCubeGameChangeTimeToStart extends ServerPacket
{
	private final int _seconds;

	public ExCubeGameChangeTimeToStart(int seconds)
	{
		this._seconds = seconds;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BLOCK_UPSET_LIST.writeId(this, buffer);
		buffer.writeInt(3);
		buffer.writeInt(this._seconds);
	}
}
