package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class SunRise extends ServerPacket
{
	public static final SunRise STATIC_PACKET = new SunRise();

	private SunRise()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.SUN_RISE.writeId(this, buffer);
	}
}
