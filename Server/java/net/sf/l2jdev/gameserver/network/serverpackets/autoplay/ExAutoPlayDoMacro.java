package net.sf.l2jdev.gameserver.network.serverpackets.autoplay;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExAutoPlayDoMacro extends ServerPacket
{
	public static final ExAutoPlayDoMacro STATIC_PACKET = new ExAutoPlayDoMacro();

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_AUTOPLAY_DO_MACRO.writeId(this, buffer);
		buffer.writeInt(276);
	}
}
