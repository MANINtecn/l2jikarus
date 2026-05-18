package net.sf.l2jdev.gameserver.network.serverpackets.prison;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExPrisonUserExit extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_PRISON_USER_EXIT.writeId(this, buffer);
		buffer.writeInt(0);
	}
}
