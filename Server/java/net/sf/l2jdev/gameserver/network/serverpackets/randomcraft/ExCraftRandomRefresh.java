package net.sf.l2jdev.gameserver.network.serverpackets.randomcraft;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCraftRandomRefresh extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_CRAFT_RANDOM_REFRESH.writeId(this, buffer);
		buffer.writeByte(0);
	}
}
