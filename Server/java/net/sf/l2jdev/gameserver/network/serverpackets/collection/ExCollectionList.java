package net.sf.l2jdev.gameserver.network.serverpackets.collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCollectionList extends ServerPacket
{
	private final int _category;

	public ExCollectionList(int category)
	{
		this._category = category;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COLLECTION_LIST.writeId(this, buffer);
		buffer.writeByte(this._category);
		buffer.writeInt(0);
		buffer.writeInt(0);
	}
}
