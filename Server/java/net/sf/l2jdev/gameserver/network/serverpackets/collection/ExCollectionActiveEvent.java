package net.sf.l2jdev.gameserver.network.serverpackets.collection;

import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.CollectionDataHolder;
import net.sf.l2jdev.gameserver.data.xml.CollectionData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCollectionActiveEvent extends ServerPacket
{
	private final List<CollectionDataHolder> _collections = CollectionData.getInstance().getCollectionsByTabId(7);

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COLLECTION_ACTIVE_EVENT.writeId(this, buffer);
		buffer.writeInt(this._collections.size());

		for (CollectionDataHolder collection : this._collections)
		{
			buffer.writeShort(collection.getCollectionId());
		}
	}
}
