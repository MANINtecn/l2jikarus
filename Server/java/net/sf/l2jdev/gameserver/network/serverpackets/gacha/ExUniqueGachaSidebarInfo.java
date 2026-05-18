package net.sf.l2jdev.gameserver.network.serverpackets.gacha;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExUniqueGachaSidebarInfo extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_UNIQUE_GACHA_SIDEBAR_INFO.writeId(this, buffer);
		buffer.writeByte(1);
	}
}
