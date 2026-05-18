package net.sf.l2jdev.gameserver.network.serverpackets.balok;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class BalrogWarGetReward extends ServerPacket
{
	private final boolean _available;

	public BalrogWarGetReward(boolean available)
	{
		this._available = available;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_BALROGWAR_GET_REWARD.writeId(this, buffer);
		buffer.writeByte(this._available);
	}
}
