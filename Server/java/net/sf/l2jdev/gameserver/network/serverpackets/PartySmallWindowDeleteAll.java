package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class PartySmallWindowDeleteAll extends ServerPacket
{
	public static final PartySmallWindowDeleteAll STATIC_PACKET = new PartySmallWindowDeleteAll();

	private PartySmallWindowDeleteAll()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.PARTY_SMALL_WINDOW_DELETE_ALL.writeId(this, buffer);
	}
}
