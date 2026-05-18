package net.sf.l2jdev.gameserver.network.serverpackets.collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCollectionCloseUI extends ServerPacket
{
	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COLLECTION_CLOSE_UI.writeId(this, buffer);
		buffer.writeByte(0);
	}
}
