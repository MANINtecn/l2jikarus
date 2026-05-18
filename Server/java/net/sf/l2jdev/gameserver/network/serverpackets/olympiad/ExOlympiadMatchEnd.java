package net.sf.l2jdev.gameserver.network.serverpackets.olympiad;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExOlympiadMatchEnd extends ServerPacket
{
	public static final ExOlympiadMatchEnd STATIC_PACKET = new ExOlympiadMatchEnd();

	private ExOlympiadMatchEnd()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_OLYMPIAD_MATCH_END.writeId(this, buffer);
	}
}
