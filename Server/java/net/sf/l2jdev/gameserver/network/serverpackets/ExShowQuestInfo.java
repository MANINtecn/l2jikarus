package net.sf.l2jdev.gameserver.network.serverpackets;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;

public class ExShowQuestInfo extends ServerPacket
{
	public static final ExShowQuestInfo STATIC_PACKET = new ExShowQuestInfo();

	private ExShowQuestInfo()
	{
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_SHOW_QUEST_INFO.writeId(this, buffer);
	}
}
