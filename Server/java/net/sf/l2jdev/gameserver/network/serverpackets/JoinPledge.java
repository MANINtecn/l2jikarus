package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class JoinPledge extends ServerPacket
{
	private final int _pledgeId;

	public JoinPledge(int pledgeId)
	{
		this._pledgeId = pledgeId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.JOIN_PLEDGE.writeId(this, buffer);
		buffer.writeInt(this._pledgeId);
	}
}
