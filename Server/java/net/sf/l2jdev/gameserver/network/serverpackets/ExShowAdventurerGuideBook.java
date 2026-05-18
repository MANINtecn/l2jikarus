package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowAdventurerGuideBook extends ServerPacket
{
	public static final ExShowAdventurerGuideBook STATIC_PACKET = new ExShowAdventurerGuideBook();

	private ExShowAdventurerGuideBook()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_ADVENTURER_GUIDE_BOOK.writeId(this, buffer);
	}
}
