package net.sf.l2jdev.gameserver.network.serverpackets.pledgeV3;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPledgeLevelUp extends ServerPacket
{
	private final int _level;

	public ExPledgeLevelUp(int level)
	{
		this._level = level;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PLEDGE_LEVEL_UP.writeId(this, buffer);
		buffer.writeInt(this._level);
	}
}
