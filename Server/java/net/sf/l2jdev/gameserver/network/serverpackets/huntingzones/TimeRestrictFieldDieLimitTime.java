package net.sf.l2jdev.gameserver.network.serverpackets.huntingzones;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class TimeRestrictFieldDieLimitTime extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_TIME_RESTRICT_FIELD_DIE_LIMT_TIME.writeId(this, buffer);
		buffer.writeInt(600);
	}
}
