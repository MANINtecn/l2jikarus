package net.sf.l2jdev.gameserver.network.serverpackets.collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCollectionComplete extends ServerPacket
{
	private final int _collectionId;

	public ExCollectionComplete(int collectionId)
	{
		this._collectionId = collectionId;
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COLLECTION_COMPLETE.writeId(this, buffer);
		buffer.writeShort(this._collectionId);
		buffer.writeInt(0);
	}
}
