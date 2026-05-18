package net.sf.l2jdev.gameserver.network.serverpackets.collection;

import java.util.Collection;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.holders.CollectionDataHolder;
import net.sf.l2jdev.gameserver.data.xml.CollectionData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExCollectionSummary extends ServerPacket
{
	private final Collection<CollectionDataHolder> _collections = CollectionData.getInstance().getCollections();

	public ExCollectionSummary(Player player)
	{
	}

	@Override
	protected void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_COLLECTION_SUMMARY.writeId(this, buffer);
		buffer.writeInt(this._collections.size());

		for (CollectionDataHolder collection : this._collections)
		{
			buffer.writeShort(collection.getCollectionId());
			buffer.writeInt(0);
		}
	}
}
