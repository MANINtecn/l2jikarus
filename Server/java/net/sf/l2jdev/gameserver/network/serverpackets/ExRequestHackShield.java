package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExRequestHackShield extends ServerPacket
{
	public static final ExRequestHackShield STATIC_PACKET = new ExRequestHackShield();

	private ExRequestHackShield()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_REQUEST_HACK_SHIELD.writeId(this, buffer);
	}
}
