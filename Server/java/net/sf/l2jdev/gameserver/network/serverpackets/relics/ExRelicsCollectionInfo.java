package net.sf.l2jdev.gameserver.network.serverpackets.relics;

import java.util.ArrayList;
import java.util.List;

import net.sf.l2jdev.commons.network.WritableBuffer;
import net.sf.l2jdev.gameserver.data.xml.RelicCollectionData;
import net.sf.l2jdev.gameserver.model.actor.Player;
import net.sf.l2jdev.gameserver.model.actor.holders.player.PlayerRelicCollectionData;
import net.sf.l2jdev.gameserver.network.GameClient;
import net.sf.l2jdev.gameserver.network.ServerPackets;
import net.sf.l2jdev.gameserver.network.serverpackets.ServerPacket;

public class ExRelicsCollectionInfo extends ServerPacket
{
	private final Player _player;
	private final List<Integer> _relicCollectionIds = new ArrayList<>();

	public ExRelicsCollectionInfo(Player player)
	{
		this._player = player;
		this._relicCollectionIds.clear();

		for (PlayerRelicCollectionData relicCollection : player.getRelicCollections())
		{
			if (!this._relicCollectionIds.contains(relicCollection.getRelicCollectionId()))
			{
				this._relicCollectionIds.add(relicCollection.getRelicCollectionId());
			}
		}
	}

	@Override
	public void writeImpl(GameClient client, WritableBuffer buffer)
	{
		ServerPackets.EX_RELICS_COLLECTION_INFO.writeId(this, buffer);
		buffer.writeInt(1);
		buffer.writeInt(82);
		buffer.writeInt(this._relicCollectionIds.size());

		for (int i = 1; i <= this._relicCollectionIds.size(); i++)
		{
			buffer.writeInt(this._relicCollectionIds.get(i - 1));
			int completeCount = 0;

			for (PlayerRelicCollectionData coll : this._player.getRelicCollections())
			{
				if (coll.getRelicCollectionId() == this._relicCollectionIds.get(i - 1))
				{
					completeCount++;
				}
			}

			int completedRelicsCount = RelicCollectionData.getInstance().getRelicCollection(this._relicCollectionIds.get(i - 1)).getCompleteCount();
			buffer.writeByte(completeCount == completedRelicsCount);
			buffer.writeInt(completeCount);

			for (PlayerRelicCollectionData collection : this._player.getRelicCollections())
			{
				if (collection.getRelicCollectionId() == this._relicCollectionIds.get(i - 1))
				{
					buffer.writeInt(collection.getRelicId());
					buffer.writeInt(collection.getRelicLevel());
				}
			}
		}
	}
}
