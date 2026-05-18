package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExClosePartyRoom extends ServerPacket
{
	public static final ExClosePartyRoom STATIC_PACKET = new ExClosePartyRoom();

	private ExClosePartyRoom()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_DISMISS_PARTY_ROOM.writeId(this, buffer);
	}
}
