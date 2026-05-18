package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowSeedMapInfo extends ServerPacket
{
	public static final ExShowSeedMapInfo STATIC_PACKET = new ExShowSeedMapInfo();

	private ExShowSeedMapInfo()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_SEED_MAP_INFO.writeId(this, buffer);
		buffer.writeInt(2);
		buffer.writeInt(1);
		buffer.writeInt(2771);
		buffer.writeInt(2);
		buffer.writeInt(2766);
	}
}
