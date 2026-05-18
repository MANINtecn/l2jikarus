package net.sf.l2jdev.gameserver.network.serverpackets.prison;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPrisonUserEnter extends ServerPacket
{
	private final int _prisonType;

	public ExPrisonUserEnter(int prisonType)
	{
		this._prisonType = prisonType;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PRISON_USER_ENTER.writeId(this, buffer);
		buffer.writeInt(this._prisonType);
	}
}
